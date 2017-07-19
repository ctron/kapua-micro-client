/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package de.dentrassi.kapua.micro.client;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PahoTransport implements Transport, AutoCloseable {

    public static class Options {

        private final String brokerUri;
        private final String clientId;
        private final String username;
        private final char[] password;

        public Options(final String brokerUri, final String clientId) {
            this(brokerUri, clientId, null, null);
        }

        public Options(final String brokerUri, final String clientId, final String username, final char[] password) {
            this.brokerUri = brokerUri;
            this.clientId = clientId;
            this.username = username;
            this.password = password;
        }

        public String getBrokerUri() {
            return this.brokerUri;
        }

        public String getClientId() {
            return this.clientId;
        }

        public String getUsername() {
            return this.username;
        }

        public char[] getPassword() {
            return this.password;
        }
    }

    private final PayloadFormat format;
    private final MqttConnectOptions options;
    private final MqttAsyncClient client;
    private final Map<String, Handler> subscriptions = new HashMap<>();
    private final Map<String, FutureTask<Void>> pendingSubscribes = new HashMap<>();
    private final Map<String, FutureTask<Void>> pendingUnsubscribes = new HashMap<>();

    private final Thread runner = new Thread(new Runnable() {

        @Override
        public void run() {
            runner();
        }
    });
    private boolean closed;

    private boolean resubscribe;
    private final TransportListener listener;

    public PahoTransport(final Options options, final PayloadFormat format, final TransportListener listener) throws Exception {
        this.options = convertOptions(options);

        this.format = format;
        this.listener = listener;

        this.client = new MqttAsyncClient(options.getBrokerUri(), options.getClientId());
        this.client.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                handleMessage(topic, message);
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken token) {
            }

            @Override
            public void connectionLost(final Throwable cause) {
                wakeup();
            }
        });

        this.runner.start();
    }

    protected void handleMessage(final String topic, final MqttMessage message) {
        final Handler handler;

        synchronized (this) {
            handler = this.subscriptions.get(topic);
        }

        if (handler != null) {
            try {
                handler.handleMessage(this.format.decode(message.getPayload()));
            } catch (final Exception e) {
            }
        }
    }

    private void runner() {

        boolean running = true;
        int delay = 0;
        boolean connecting = true;

        while (running) {
            synchronized (this) {

                try {
                    if (delay > 0) {
                        wait(delay);
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                delay = 10_000;

                if (this.closed) {
                    running = false;
                    continue;
                }

                if (!this.client.isConnected()) {

                    if (!connecting && this.listener != null) {
                        try {
                            this.listener.disconnected();
                        } catch (final Exception e) {
                        }
                    }

                    // flush waiting subscribes .. we are not connected, and thus not subscribed already

                    flushTasks(this.pendingUnsubscribes);

                    // start connecting

                    connecting = true;

                    try {
                        this.client.connect(this.options, null, new IMqttActionListener() {

                            @Override
                            public void onSuccess(final IMqttToken asyncActionToken) {
                                new Thread() {

                                    @Override
                                    public void run() {
                                        synchronized (PahoTransport.this) {
                                            PahoTransport.this.resubscribe = true;
                                            wakeup();
                                        }
                                    }
                                }.start();
                            }

                            @Override
                            public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                            }
                        });
                    } catch (final MqttException e) {
                        continue;
                    }
                } else {

                    connecting = false;

                    if (this.resubscribe) {
                        this.resubscribe = false;

                        final int len = this.subscriptions.size();
                        if (len > 0) {
                            final String[] topics = new String[len];
                            final int[] qos = new int[topics.length];
                            int i = 0;

                            for (final String topic : this.subscriptions.keySet()) {
                                topics[i] = topic;
                                qos[i] = 0;
                                i++;
                            }
                            try {
                                this.client.subscribe(topics, qos);

                            } catch (final MqttException e) {
                            }
                        }

                        // flush waiting subscribes

                        flushTasks(this.pendingSubscribes);

                        // notify transport listener

                        if (this.listener != null) {
                            try {
                                this.listener.connected();
                            } catch (final Exception e) {
                            }
                        }
                    }

                    if (!this.pendingSubscribes.isEmpty()) {
                        for (final Map.Entry<String, FutureTask<Void>> entry : this.pendingSubscribes.entrySet()) {
                            try {
                                this.client.subscribe(entry.getKey(), 0, null, reportTo(entry.getValue()));
                            } catch (final MqttException e) {
                                entry.getValue().completedExceptionally(e);
                            }
                        }
                        this.pendingSubscribes.clear();
                    }

                    if (!this.pendingUnsubscribes.isEmpty()) {
                        for (final Map.Entry<String, FutureTask<Void>> entry : this.pendingUnsubscribes.entrySet()) {
                            try {
                                this.client.unsubscribe(entry.getKey(), null, reportTo(entry.getValue()));
                            } catch (final MqttException e) {
                                entry.getValue().completedExceptionally(e);
                            }
                        }
                        this.pendingUnsubscribes.clear();
                    }

                }
            }
        }

        if (this.client.isConnected()) {
            try {
                this.client.disconnect();
            } catch (final MqttException e) {
            }
        }

        // clean up
        try {
            this.client.close();
        } catch (final MqttException e) {
        }
    }

    private static void flushTasks(final Map<String, FutureTask<Void>> tasks) {
        for (final FutureTask<Void> task : tasks.values()) {
            try {
                task.completed(null);
            } catch (final Exception e) {
            }
        }

        tasks.clear();
    }

    private synchronized void wakeup() {
        notifyAll();
    }

    @Override
    public Future<Void> publish(final String topic, final Payload payload) {
        final FutureTask<Void> task = new FutureTask<>();

        try {
            this.client.publish(topic, this.format.encode(payload), 1, false, null, reportTo(task));
        } catch (final Exception e) {
            task.completedExceptionally(e);
        }

        return task;
    }

    @Override
    public Future<Void> subscribe(final String topic, final Handler handler) {
        final FutureTask<Void> task = new FutureTask<>();

        final FutureTask<Void> oldTask;

        synchronized (this) {
            if (this.subscriptions.put(topic, handler) != null) {
                // already subscribed
                task.completed(null);
                return task;
            }

            this.pendingSubscribes.put(topic, task);
            oldTask = this.pendingUnsubscribes.remove(topic);

            wakeup();
        }

        if (oldTask != null) {
            oldTask.completed(null);
        }

        return task;
    }

    @Override
    public Future<Void> unsubscribe(final String topic) {
        final FutureTask<Void> task = new FutureTask<>();

        final FutureTask<Void> oldTask;

        synchronized (this) {
            if (this.subscriptions.remove(topic) == null) {
                // already subscribed
                task.completed(null);
                return task;
            }

            this.pendingUnsubscribes.put(topic, task);
            oldTask = this.pendingSubscribes.remove(topic);

            wakeup();
        }

        if (oldTask != null) {
            oldTask.completed(null);
        }

        return task;
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            this.closed = true;
            wakeup();
        }
    }

    public void waitForClose() throws InterruptedException {
        this.runner.join();
    }

    private static MqttConnectOptions convertOptions(final Options options) {
        final MqttConnectOptions result = new MqttConnectOptions();

        result.setUserName(options.getUsername());
        result.setPassword(options.getPassword());

        return result;
    }

    private static IMqttActionListener reportTo(final FutureTask<Void> task) {
        return new IMqttActionListener() {

            @Override
            public void onSuccess(final IMqttToken asyncActionToken) {
                if (task != null) {
                    task.completed(null);
                }
            }

            @Override
            public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                if (task != null) {
                    task.completedExceptionally(exception);
                }
            }
        };
    }

}
