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

public class MqttTransportOptions {

    private String brokerUri;
    private String clientId;
    private String username;
    private char[] password;

    private boolean cleanSession = false;

    public MqttTransportOptions(final String brokerUri, final String clientId) {
        this(brokerUri, clientId, null, (char[]) null);
    }

    public MqttTransportOptions(final String brokerUri, final String clientId, final String username, final char[] password) {
        this.brokerUri = brokerUri;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    public MqttTransportOptions(final String brokerUri, final String clientId, final String username, final String password) {
        this.brokerUri = brokerUri;
        this.clientId = clientId;
        this.username = username;
        this.password = password != null ? password.toCharArray() : null;
    }

    public void setBrokerUri(final String brokerUri) {
        this.brokerUri = brokerUri;
    }

    public String getBrokerUri() {
        return this.brokerUri;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setPassword(final char[] password) {
        this.password = password;
    }

    public void setPassword(final String password) {
        this.password = password != null ? password.toCharArray() : null;
    }

    public char[] getPassword() {
        return this.password;
    }

    public void setCleanSession(final boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isCleanSession() {
        return this.cleanSession;
    }
}