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
import de.dentrassi.kapua.micro.client.KuraNamespace;
import de.dentrassi.kapua.micro.client.KuraProtobufFormat;
import de.dentrassi.kapua.micro.client.MicroApplication;
import de.dentrassi.kapua.micro.client.MicroClient;
import de.dentrassi.kapua.micro.client.PahoTransport;
import de.dentrassi.kapua.micro.client.PahoTransport.Options;
import de.dentrassi.kapua.micro.client.Payload;
import de.dentrassi.kapua.micro.client.Topic;

public class TestApp1 {

    public static void main(final String[] args) throws Exception {

        final Options options = new Options("tcp://iot.eclipse.org:1883", "foo-bar");

        try (final PahoTransport transport = new PahoTransport(options, new KuraProtobufFormat(), null)) {
            try (final MicroClient client = new MicroClient(transport, new KuraNamespace("kapua-sys", "micro-1"))) {

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

        } // transport

        System.out.println("Exiting...");
    }
}
