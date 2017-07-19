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

import org.junit.Assert;
import org.junit.Test;

import de.dentrassi.kapua.micro.client.FutureTask;
import de.dentrassi.kapua.micro.client.Result;

public class FutureTaskTest {

    @Test(timeout = 1_000)
    public void testGet1() throws InterruptedException {
        final FutureTask<Void> f1 = new FutureTask<>();

        f1.completed(null);

        f1.get();
    }

    @Test(timeout = 1_000)
    public void testGet2() throws InterruptedException {
        final FutureTask<Void> f1 = new FutureTask<>();

        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                }
                f1.completed(null);
            };
        }.start();

        f1.get();
    }

    @Test(timeout = 1_000)
    public void testGet3() throws InterruptedException {
        final FutureTask<Void> f1 = new FutureTask<>();

        final long start = System.currentTimeMillis();

        final Result<Void> result = f1.get(500);

        final long duration = System.currentTimeMillis() - start;

        Assert.assertNull(result);
        Assert.assertTrue(duration >= 500);
    }
}
