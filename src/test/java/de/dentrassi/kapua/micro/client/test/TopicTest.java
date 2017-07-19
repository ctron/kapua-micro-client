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

import de.dentrassi.kapua.micro.client.Topic;

public class TopicTest {

    @Test
    public void test1() {
        Assert.assertEquals("foo/bar", Topic.of("foo", "bar").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2() {
        Topic.of("foo/bar");
    }
}
