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
package de.dentrassi.kapua.micro.client.test;

import de.dentrassi.kapua.micro.client.Handler;
import de.dentrassi.kapua.micro.client.Payload;
import de.dentrassi.kapua.micro.client.format.KuraProtobufFormat;
import de.dentrassi.kapua.micro.client.transport.MqttTransportOptions;
import de.dentrassi.kapua.micro.client.transport.PahoTransport;
import de.dentrassi.kapua.micro.client.transport.TransportListener;
import de.dentrassi.kapua.micro.client.util.Nothing;
import de.dentrassi.kapua.micro.client.util.Result;
import de.dentrassi.kapua.micro.client.util.ResultHandler;

public class TestTransport {

    public static void main(final String[] args) throws Exception {

        final MqttTransportOptions options = new MqttTransportOptions("tcp://iot.eclipse.org:1883", "foo-bar");

        try (final PahoTransport transport = PahoTransport.creator(options, KuraProtobufFormat.defaultInstance()).createTransport(new TransportListener() {

            @Override
            public void disconnected() {
                System.out.println("Disconnected");
            }

            @Override
            public void connected() {
                System.out.println("Connected");
            }
        })) {

            System.out.println("Waiting...");
            Thread.sleep(1_000);
            System.out.println("Waiting...");

            transport.subscribe("my/foo/bar", new Handler() {

                @Override
                public void handleMessage(final Payload payload) {
                    System.out.println("Got mail: " + payload);
                }
            }).handle(new ResultHandler<Nothing>() {

                @Override
                public void completed(final Result<Nothing> result) {
                    System.out.println("Subscribe completed: " + result);
                }
            });

            transport.subscribe("my/foo/bar/2", new Handler() {

                @Override
                public void handleMessage(final Payload payload) {
                    System.out.println("Got mail 2: " + payload);
                }
            }).handle(new ResultHandler<Nothing>() {

                @Override
                public void completed(final Result<Nothing> result) {
                    System.out.println("Subscribe completed: " + result);
                }
            });

            transport.publish("my/foo/bar", new Payload.Builder().build()).handle(new ResultHandler<Nothing>() {

                @Override
                public void completed(final Result<Nothing> result) {
                    System.out.println("Publish completed: " + result);

                }
            });

            int i = 10;
            while (i > 0) {
                transport.publish("my/foo/bar/2", new Payload.Builder().build()).handle(new ResultHandler<Nothing>() {

                    @Override
                    public void completed(final Result<Nothing> result) {
                        System.out.println("Publish completed: " + result);

                    }
                });

                Thread.sleep(1_000);
                i--;
            }

            System.out.println("Unsubscribe");

            transport.unsubscribe("my/foo/bar/2");

            transport.publish("my/foo/bar/2", new Payload.Builder().build()).handle(new ResultHandler<Nothing>() {

                @Override
                public void completed(final Result<Nothing> result) {
                    System.out.println("Publish completed: " + result);

                }
            });

            Thread.sleep(5_000);
            System.out.println("Waiting... done!");

        }

        System.out.println("Exiting...");
    }
}
