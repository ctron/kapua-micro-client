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
import de.dentrassi.kapua.micro.client.lifecycle.BirthCertificateProviders;
import de.dentrassi.kapua.micro.client.namespace.KuraNamespace;
import de.dentrassi.kapua.micro.client.transport.MqttTransportOptions;
import de.dentrassi.kapua.micro.client.transport.PahoTransport;

public class TestApp1 {

    public static void main(final String[] args) throws Exception {

        final MqttTransportOptions options = new MqttTransportOptions("tcp://iot.eclipse.org:1883", "foo-bar");

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

                final Topic t1 = Topic.of("clicked");

                app.subscribe(t1, new Handler() {

                    @Override
                    public void handleMessage(final Payload payload) {
                        System.out.println("T1: " + payload);
                    }
                }).get();

                Thread.sleep(10_000);

            } // app

        } // client

        System.out.println("Exiting...");
    }
}
