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

import java.util.HashSet;
import java.util.Set;

import de.dentrassi.kapua.micro.client.util.Future;
import de.dentrassi.kapua.micro.client.util.FutureTask;
import de.dentrassi.kapua.micro.client.util.Nothing;

/**
 * An application instance
 */
public class MicroApplication implements AutoCloseable {

    private final MicroClient client;
    private final String name;
    private final Set<Topic> subscriptions = new HashSet<>();

    MicroApplication(final MicroClient client, final String name) {
        this.client = client;
        this.name = name;
    }

    public Future<Nothing> publish(final Topic topic, final Payload payload) {
        return this.client.publish(this.name, topic, payload);
    }

    public Future<Nothing> subscribe(final Topic topic, final Handler handler) {
        if (this.subscriptions.add(topic)) {
            return this.client.subscribe(this.name, topic, handler);
        } else {
            return new FutureTask<Nothing>().completed(null);
        }
    }

    @Override
    public void close() throws Exception {
        this.client.closeApplication(this.name, this.subscriptions);
    }
}
