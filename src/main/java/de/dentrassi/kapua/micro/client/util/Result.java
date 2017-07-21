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

public class Result<T> {

    private final T data;
    private final Throwable error;

    private Result(final T data, final Throwable error) {
        this.data = data;
        this.error = error;
    }

    public T get() {
        return this.data;
    }

    public boolean isError() {
        return this.error != null;
    }

    public Throwable getError() {
        return this.error;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[Result - ");
        if (this.error == null) {
            sb.append("OK: ");
            sb.append(this.data);
        } else {
            sb.append("ERROR: ");
            sb.append(this.error.getMessage());
        }
        sb.append(']');
        return sb.toString();
    }

    public static <T> Result<T> ok(final T result) {
        return new Result<>(result, null);
    }

    public static <T> Result<T> error(final Throwable error) {
        return new Result<>(null, error);
    }
}
