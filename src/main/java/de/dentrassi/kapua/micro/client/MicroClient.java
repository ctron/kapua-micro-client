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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MicroClient implements AutoCloseable {

    private final Namespace namespace;
    private final BirthCertificateProvider[] birthCertificateProviders;
    private final Transport transport;

    private final Map<String, MicroApplication> applications = new HashMap<>();
    private final TransportListener listener = new TransportListener() {

        @Override
        public void connected() {
            handleConnnected();
        }

        @Override
        public void disconnected() {
            handleDisconnected();
        }

    };

    public MicroClient(final Namespace namespace, final BirthCertificateProvider[] birthCertificateProviders, final TransportCreator<? extends Transport> transportCreator) throws Exception {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(transportCreator);

        this.namespace = namespace;
        this.birthCertificateProviders = birthCertificateProviders;
        this.transport = transportCreator.createTransport(this.listener);
    }

    public synchronized MicroApplication createApplication(final String name) {
        Objects.requireNonNull(name);

        if (this.applications.containsKey(name)) {
            throw new IllegalStateException("Application already exists:" + name);
        }

        final MicroApplication result = new MicroApplication(this, name);

        this.applications.put(name, result);

        sendBirthCertificate();

        return result;
    }

    Future<Nothing> publish(final String applicationName, final Topic topic, final Payload payload) {
        return this.transport.publish(this.namespace.data(applicationName, topic), payload);
    }

    Future<Nothing> subscribe(final String applicationName, final Topic topic, final Handler handler) {
        return this.transport.subscribe(this.namespace.data(applicationName, topic), handler);
    }

    @Override
    public void close() throws Exception {
        if (this.transport != null) {
            this.transport.close();
        }
    }

    protected void handleConnnected() {
        sendBirthCertificate();
    }

    protected void handleDisconnected() {
    }

    protected void sendBirthCertificate() {

        // get topic

        final String topic = this.namespace.birth();
        if (topic == null) {
            return;
        }

        final Payload.Builder payload = new Payload.Builder();

        // gather payload data

        if (this.birthCertificateProviders != null) {
            for (final BirthCertificateProvider provider : this.birthCertificateProviders) {
                if (provider == null) {
                    continue;
                }
                provider.provide(payload);
            }
        }

        // set the application IDs

        {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (final String appId : this.applications.keySet()) {
                if (!first) {
                    sb.append(',');
                } else {
                    first = false;
                }
                sb.append(appId);
            }

            payload.metric("application_ids", sb.toString());
        }

        // send out

        this.transport.publish(topic, payload.build());
    }

    synchronized void closeApplication(final String name, final Collection<Topic> topics) {

        if (this.applications.remove(name) == null) {
            return;
        }

        // unsubscribe from all subscriptions

        if (!topics.isEmpty()) {
            for (final Topic topic : topics) {
                this.transport.unsubscribe(this.namespace.data(name, topic));
            }
        }
    }

}
