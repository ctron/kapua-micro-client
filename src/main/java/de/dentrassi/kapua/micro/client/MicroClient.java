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

public class MicroClient implements AutoCloseable {

    private final Transport transport;
    private final Namespace namespace;

    private final Map<String, MicroApplication> applications = new HashMap<>();

    public MicroClient(final Transport transport, final Namespace namespace) {
        this.transport = transport;
        this.namespace = namespace;
    }

    public synchronized MicroApplication createApplication(final String name) {
        if (this.applications.containsKey(name)) {
            throw new IllegalStateException("Application already exists:" + name);
        }

        final MicroApplication result = new MicroApplication(this, name);

        this.applications.put(name, result);

        // FIXME: send birth

        return result;
    }

    Future<Void> publish(final String applicationName, final Topic topic, final Payload payload) {
        return this.transport.publish(this.namespace.data(applicationName, topic), payload);
    }

    Future<Void> subscribe(final String applicationName, final Topic topic, final Handler handler) {
        return this.transport.subscribe(this.namespace.data(applicationName, topic), handler);
    }

    @Override
    public void close() {
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
