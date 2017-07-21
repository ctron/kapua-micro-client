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

public class FutureTask<T> implements Future<T> {

    private ResultHandler<T> handler;

    private boolean fired;

    private Result<T> result;

    public FutureTask<T> completed(final T result) {
        completeWith(Result.ok(result));
        return this;
    }

    public FutureTask<T> completedExceptionally(final Throwable error) {
        completeWith(Result.<T> error(error));
        return this;
    }

    private void completeWith(final Result<T> result) {
        final ResultHandler<T> handler;

        synchronized (this) {
            if (this.fired) {
                // we already fired, and we only do this once
                throw new IllegalStateException("Future is already completed");
            }

            if (this.handler == null) {
                this.result = result;
                handler = null;
                // nothing to do right now
            } else {
                // mark as fired
                this.fired = true;
                handler = this.handler;
            }

            notifyAll();
        }

        if (this.fired && handler != null) {
            // it could only be us ... so let's do it
            handler.completed(result);
        }
    }

    @Override
    public void handle(final ResultHandler<T> handler) {
        boolean needToFire = false;

        synchronized (this) {
            if (this.fired) {
                // we only fire once
                return;
            }

            if (this.handler != null) {
                throw new IllegalStateException("Handler is already set");
            }

            if (this.result != null) {
                needToFire = true;
                this.fired = true;
            }
        }

        if (needToFire) {
            this.handler.completed(this.result);
        }

    }

    @Override
    public Result<T> get() throws InterruptedException {
        synchronized (this) {
            while (this.result == null) {
                wait();
            }
        }
        return this.result;
    }

    @Override
    public Result<T> get(final long timeout) throws InterruptedException {

        final long end = System.currentTimeMillis() + timeout;

        synchronized (this) {
            while (this.result == null) {
                final long rem = end - System.currentTimeMillis();
                if (rem <= 0) {
                    return null;
                }
                wait(rem);
            }
        }
        return this.result;
    }

}
