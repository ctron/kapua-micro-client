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

import de.dentrassi.kapua.micro.client.BirthCertificateProvider;
import de.dentrassi.kapua.micro.client.BirthCertificateProviders;
import de.dentrassi.kapua.micro.client.KuraNamespace;
import de.dentrassi.kapua.micro.client.KuraProtobufFormat;
import de.dentrassi.kapua.micro.client.MicroApplication;
import de.dentrassi.kapua.micro.client.MicroClient;
import de.dentrassi.kapua.micro.client.MqttTransportOptions;
import de.dentrassi.kapua.micro.client.PahoTransport;
import de.dentrassi.kapua.micro.client.Payload;
import de.dentrassi.kapua.micro.client.Topic;

public class TestApp3 {

    public static void main(final String[] args) throws Exception {

        final MqttTransportOptions options = new MqttTransportOptions("tcp://localhost:1883", "foo-bar", "kapua-broker", "kapua-password");

        final BirthCertificateProvider[] providers = new BirthCertificateProvider[] {
                BirthCertificateProviders.jvm(),
                BirthCertificateProviders.os(),
                BirthCertificateProviders.runtime()
        };

        try (final MicroClient client = new MicroClient(new KuraNamespace("kapua-sys", options),
                providers,
                PahoTransport.creator(options, KuraProtobufFormat.defaultInstance()))) {

            try (final MicroApplication app = client.createApplication("app1")) {

                System.out.println("Application registered");

                final Topic t1 = Topic.of("dummy");

                for (int i = 0; i < 10; i++) {
                    app.publish(t1, new Payload.Builder()
                            .metric("foo", "bar")
                            .metric("count", i)
                            .build());
                    Thread.sleep(1_000);
                }

            } // app

        } // client

        System.out.println("Exiting...");
    }
}
