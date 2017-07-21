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

import de.dentrassi.kapua.micro.client.Payload.Builder;

public final class BirthCertificateProviders {

    private static final BirthCertificateProvider JVM;
    private static final BirthCertificateProvider OS;
    private static final BirthCertificateProvider RUNTIME;

    static {

        final Map<String, Object> jvmValues = new HashMap<>();
        Properties.addWhenPresent(jvmValues, "java.vm.name", "jvm_name");
        Properties.addWhenPresent(jvmValues, "java.version", "jvm_version");

        JVM = new BirthCertificateProvider() {

            @Override
            public void provide(final Builder payload) {
                payload.metrics(jvmValues);
            }
        };

        final Map<String, Object> osValues = new HashMap<>();
        Properties.addWhenPresent(osValues, "os.name", "os");
        Properties.addWhenPresent(osValues, "os.version", "os_version");
        Properties.addWhenPresent(osValues, "os.arch", "os_arch");

        OS = new BirthCertificateProvider() {

            @Override
            public void provide(final Builder payload) {
                payload.metrics(osValues);
            }
        };

        final Map<String, Object> runtimeValues = new HashMap<>();
        runtimeValues.put("total_memory", Long.toString(Runtime.getRuntime().totalMemory()));
        runtimeValues.put("max_memory", Long.toString(Runtime.getRuntime().maxMemory()));

        RUNTIME = new BirthCertificateProvider() {

            @Override
            public void provide(final Builder payload) {
                payload.metrics(runtimeValues);
            }
        };
    }

    private BirthCertificateProviders() {
    }

    public static BirthCertificateProvider values(final Map<String, Object> values) {
        return new BirthCertificateProvider() {

            @Override
            public void provide(final Builder payload) {
                payload.metrics(values);
            }
        };
    }

    public static BirthCertificateProvider jvm() {
        return JVM;
    }

    public static BirthCertificateProvider os() {
        return OS;
    }

    public static BirthCertificateProvider runtime() {
        return RUNTIME;
    }
}
