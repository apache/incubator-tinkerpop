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
package org.apache.tinkerpop.gremlin.hadoop.groovy.plugin;

import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.groovy.plugin.RemoteAcceptor;
import org.apache.tinkerpop.gremlin.groovy.util.TestableConsolePluginAcceptor;
import org.apache.tinkerpop.gremlin.hadoop.HadoopGraphProvider;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.computer.KeyValue;
import org.apache.tinkerpop.gremlin.process.computer.MapReduce;
import org.apache.tinkerpop.gremlin.process.computer.traversal.step.sideEffect.mapreduce.TraverserMapReduce;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class HadoopGremlinPluginTest extends AbstractGremlinTest {

    @Before
    public void setupTest() {
        try {
            this.console = new TestableConsolePluginAcceptor();
            final HadoopGremlinPlugin plugin = new HadoopGremlinPlugin();
            plugin.pluginTo(this.console);
            this.remote = (HadoopRemoteAcceptor) plugin.remoteAcceptor().get();
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    ///////////////////

    private HadoopRemoteAcceptor remote;
    private TestableConsolePluginAcceptor console;

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldSupportRemoteTraversal() throws Exception {
        this.console.addBinding("graph", this.graph);
        this.console.addBinding("g", this.g);
        this.remote.connect(Arrays.asList("graph", "g"));
        //
        Traversal<?, ?> traversal = (Traversal<?, ?>) this.remote.submit(Arrays.asList("g.V().count()"));
        assertEquals(6L, traversal.next());
        assertFalse(traversal.hasNext());
        assertNotNull(this.console.getBindings().get(RemoteAcceptor.RESULT));
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldSupportRemoteSugarTraversal() throws Exception {
        this.console.addBinding("graph", this.graph);
        this.console.addBinding("g", this.g);
        this.remote.connect(Arrays.asList("graph"));
        //
        this.remote.configure(Arrays.asList("useSugar", "true"));
        this.remote.connect(Arrays.asList("graph", "g"));
        Traversal<?, ?> traversal = (Traversal<?, ?>) this.remote.submit(Arrays.asList("g.V.name.map{it.length()}.sum"));
        assertEquals(28.0d, traversal.next());
        assertFalse(traversal.hasNext());
        assertNotNull(this.console.getBindings().get(RemoteAcceptor.RESULT));
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldSupportHDFSMethods() throws Exception {
        ////////////////
        List<String> ls = (List<String>) this.console.eval("hdfs.ls()");
        for (final String line : ls) {
            assertTrue(line.startsWith("-") || line.startsWith("r") || line.startsWith("w") || line.startsWith("x"));
            assertEquals(" ", line.substring(9, 10));
        }
        ls = (List<String>) this.console.eval("local.ls()");
        for (final String line : ls) {
            assertTrue(line.startsWith("-") || line.startsWith("r") || line.startsWith("w") || line.startsWith("x"));
            assertEquals(" ", line.substring(9, 10));
        }
        ////////////////
        this.console.eval("hdfs.copyFromLocal('" + HadoopGraphProvider.PATHS.get("tinkerpop-classic.txt") + "', 'target/tinkerpop-classic.txt')");
        assertTrue((Boolean) this.console.eval("hdfs.exists('target/tinkerpop-classic.txt')"));
        ////////////////
        List<String> head = IteratorUtils.asList(this.console.eval("hdfs.head('target/tinkerpop-classic.txt')"));
        assertEquals(6, head.size());
        for (final String line : head) {
            assertEquals(":", line.substring(1, 2));
            assertTrue(Integer.valueOf(line.substring(0, 1)) <= 6);
        }
        head = IteratorUtils.asList(this.console.eval("hdfs.head('target/tinkerpop-classic.txt',3)"));
        assertEquals(3, head.size());
        for (final String line : head) {
            assertEquals(":", line.substring(1, 2));
            assertTrue(Integer.valueOf(line.substring(0, 1)) <= 3);
        }
        ////////////////
        this.console.eval("hdfs.rm('target/tinkerpop-classic.txt')");
        assertFalse((Boolean) this.console.eval("hdfs.exists('target/tinkerpop-classic.txt')"));
        ////////////////
        this.console.addBinding("graph", this.graph);
        this.console.addBinding("g", this.g);
        this.remote.connect(Arrays.asList("graph", "g"));
        Traversal<Vertex, String> traversal = (Traversal<Vertex, String>) this.remote.submit(Arrays.asList("g.V().hasLabel('person').group('m').by('age').by('name').out('knows').out('created').values('name')"));
        AbstractGremlinProcessTest.checkResults(Arrays.asList("ripple", "lop"), traversal);
        assertTrue((Boolean) this.console.eval("hdfs.exists('hadoop-gremlin/target/test-output/m')"));
        assertTrue((Boolean) this.console.eval("hdfs.exists('hadoop-gremlin/target/test-output/" + TraverserMapReduce.TRAVERSERS + "')"));
        final List<KeyValue<Integer, BulkSet<String>>> mList = IteratorUtils.asList(this.console.eval("hdfs.head('hadoop-gremlin/target/test-output/m',ObjectWritable)"));
        assertEquals(4, mList.size());
        mList.forEach(keyValue -> {
            if (keyValue.getKey().equals(29))
                assertEquals(1l, keyValue.getValue().get("marko"));
            else if (keyValue.getKey().equals(35))
                assertEquals(1l, keyValue.getValue().get("peter"));
            else if (keyValue.getKey().equals(32))
                assertEquals(1l, keyValue.getValue().get("josh"));
            else if (keyValue.getKey().equals(27))
                assertEquals(1l, keyValue.getValue().get("vadas"));
            else
                throw new IllegalStateException("The provided key/value is unknown: " + keyValue);
        });
        final List<KeyValue<MapReduce.NullObject, Traverser<String>>> traversersList = IteratorUtils.asList(this.console.eval("hdfs.head('hadoop-gremlin/target/test-output/" + TraverserMapReduce.TRAVERSERS + "',ObjectWritable)"));
        assertEquals(2, traversersList.size());
        traversersList.forEach(keyValue -> {
            assertEquals(MapReduce.NullObject.instance(), keyValue.getKey());
            final String name = keyValue.getValue().get();
            assertTrue(name.equals("ripple") || name.equals("lop"));
        });
        ////////////////
    }

}
