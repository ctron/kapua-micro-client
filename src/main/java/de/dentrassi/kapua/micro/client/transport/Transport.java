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
package de.dentrassi.kapua.micro.client.transport;

import de.dentrassi.kapua.micro.client.Handler;
import de.dentrassi.kapua.micro.client.Payload;
import de.dentrassi.kapua.micro.client.util.Future;
import de.dentrassi.kapua.micro.client.util.Nothing;

public interface Transport extends AutoCloseable {

    public Future<Nothing> publish(String topic, Payload payload);

    public Future<Nothing> subscribe(String topic, Handler handler);

    public Future<Nothing> unsubscribe(String topic);
}
