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
import de.dentrassi.kapua.micro.client.MicroApplication;
import de.dentrassi.kapua.micro.client.MicroClient;
import de.dentrassi.kapua.micro.client.Payload;
import de.dentrassi.kapua.micro.client.Topic;
import de.dentrassi.kapua.micro.client.format.KuraProtobufFormat;
import de.dentrassi.kapua.micro.client.lifecycle.BirthCertificateProvider;
import de.dentrassi.kapua.micro.client.namespace.KuraNamespace;
import de.dentrassi.kapua.micro.client.transport.MqttTransportOptions;
import de.dentrassi.kapua.micro.client.transport.PahoTransport;

public class TestApp2 {

    public static void main(final String[] args) throws Exception {

        final MqttTransportOptions options = new MqttTransportOptions("tcp://iot.eclipse.org:1883", "foo-bar");

        final BirthCertificateProvider[] providers = new BirthCertificateProvider[] {};

        try (final MicroClient client = new MicroClient(new KuraNamespace("kapua-sys", options),
                providers,
                PahoTransport.creator(options, KuraProtobufFormat.defaultInstance()))) {

            try (final MicroApplication app = client.createApplication("app-1")) {

                System.out.println("Application registered");

                final Topic t1 = Topic.of("data", "1");

                app.subscribe(t1, new Handler() {

                    @Override
                    public void handleMessage(final Payload payload) {
                        System.out.println("T1: " + payload);
                    }
                }).get();

                System.out.println("Subscribed");

                for (int i = 0; i < 10; i++) {
                    app.publish(t1, new Payload.Builder()
                            .metric("foo", "bar")
                            .metric("count", i)
                            .build());
                    Thread.sleep(1_000);
                }

                Thread.sleep(10_000);

            } // app

        } // client

        System.out.println("Exiting...");
    }
}
