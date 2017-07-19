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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Payload {

    public static class Builder {

        private long timestamp;

        private final Map<String, Object> metrics = new HashMap<>();

        public Builder from(final Date date) {
            if (date != null) {
                this.timestamp = date.getTime();
            } else {
                this.timestamp = System.currentTimeMillis();
            }
            return this;
        }

        public Builder from(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder fromNow() {
            this.timestamp = 0;
            return this;
        }

        public Builder metric(final String key, final String value) {
            this.metrics.put(key, value);
            return this;
        }

        public Builder metric(final String key, final int value) {
            this.metrics.put(key, value);
            return this;
        }

        public Payload build() {
            return new Payload(this.timestamp == 0 ? System.currentTimeMillis() : this.timestamp, this.metrics);
        }
    }

    private final long timestamp;
    private final Map<String, Object> metrics;

    Payload(final long timestamp, final Map<String, Object> metrics) {
        this.timestamp = timestamp;
        this.metrics = Collections.unmodifiableMap(metrics);
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Map<String, Object> getMetrics() {
        return this.metrics;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[Payload - TS: ");
        sb.append(this.timestamp);
        sb.append(" -> ");
        sb.append(this.metrics);
        sb.append(']');
        return sb.toString();
    }
}
