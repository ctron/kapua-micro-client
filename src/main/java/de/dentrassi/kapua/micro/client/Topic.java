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

public class Topic {

    private final String topic;

    private Topic(final String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return this.topic;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.topic == null ? 0 : this.topic.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Topic other = (Topic) obj;
        if (this.topic == null) {
            if (other.topic != null) {
                return false;
            }
        } else if (!this.topic.equals(other.topic)) {
            return false;
        }
        return true;
    }

    public static Topic of(final String... segments) {
        final StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (final String segment : segments) {

            checkSegment(segment);

            if (!first) {
                sb.append('/');
            } else {
                first = false;
            }
            sb.append(segment);
        }

        return new Topic(sb.toString());
    }

    private static void checkSegment(final String segment) {
        if (segment.matches(".*[\\#\\+\\/]+.*")) {
            throw new IllegalArgumentException("Topic segments must not contain special characters (#, + or /)");
        }
    }
}
