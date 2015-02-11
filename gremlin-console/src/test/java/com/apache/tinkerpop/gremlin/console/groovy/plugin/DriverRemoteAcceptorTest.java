/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.apache.tinkerpop.gremlin.console.groovy.plugin;

import com.apache.tinkerpop.gremlin.TestHelper;
import com.apache.tinkerpop.gremlin.groovy.plugin.RemoteException;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class DriverRemoteAcceptorTest {

    private final Groovysh groovysh = new Groovysh();
    private DriverRemoteAcceptor acceptor;

    @Before
    public void setUp() {
        acceptor = new DriverRemoteAcceptor(groovysh);
    }

    @After
    public void tearDown() {
        try {
            acceptor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test(expected = RemoteException.class)
    public void shouldNotConnectWithEmptyArgs() throws Exception {
        acceptor.connect(new ArrayList<>());
    }

    @Test(expected = RemoteException.class)
    public void shouldNotConnectWithTooManyArgs() throws Exception {
        acceptor.connect(Arrays.asList("two", "too", "many"));
    }

    @Test(expected = RemoteException.class)
    public void shouldNotConnectWithInvalidConfigFile() throws Exception {
        acceptor.connect(Arrays.asList("this-isnt-real.yaml"));
    }

    @Test
    public void shouldConnect() throws Exception {
        // there is no gremlin server running for this test, but gremlin-driver lazily connects so this should
        // be ok to just validate that a connection is created
        assertThat(acceptor.connect(Arrays.asList(TestHelper.generateTempFileFromResource(this.getClass(), "remote.yaml", ".tmp").getAbsolutePath())).toString(), startsWith("Connected - "));
    }
}
