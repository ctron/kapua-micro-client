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

import java.util.Objects;

public class KuraNamespace implements Namespace {

    private final String accountName;
    private final String clientId;

    public KuraNamespace(final String accountName, final MqttTransportOptions options) {
        Objects.requireNonNull(accountName);
        Objects.requireNonNull(options);
        Objects.requireNonNull(options.getClientId());

        this.accountName = accountName;
        this.clientId = options.getClientId();
    }

    @Override
    public String data(final String applicationName, final Topic topic) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.accountName).append('/');
        sb.append(this.clientId).append('/');
        sb.append(applicationName).append('/');
        sb.append(topic);
        return sb.toString();
    }

    @Override
    public String birth() {
        final StringBuilder sb = new StringBuilder("$EDC/");
        sb.append(this.accountName).append('/');
        sb.append(this.clientId).append("/MQTT/BIRTH");
        return sb.toString();
    }

}
