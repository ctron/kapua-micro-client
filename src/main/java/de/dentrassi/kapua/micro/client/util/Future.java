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
package de.dentrassi.kapua.micro.client.util;

public interface Future<T> {

    public void handle(ResultHandler<T> handler);

    public Result<T> get() throws InterruptedException;

    public Result<T> get(long timeout) throws InterruptedException;
}
