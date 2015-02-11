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
package com.tinkerpop.gremlin.process.graph.traversal.step.map;

import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.structure.Compare;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.util.StreamFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://traversalhen.genoprime.com)
 * @author Daniel Kuppitz (daniel at thinkaurelius.com)
 */
public abstract class VertexTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Vertex> get_g_V();

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_out(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX2X_in(final Object v2Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX4X_both(final Object v4Id);

    public abstract Traversal<Edge, Edge> get_g_E();

    public abstract Traversal<Vertex, Edge> get_g_VX1X_outE(final Object v1Id);

    public abstract Traversal<Vertex, Edge> get_g_VX2X_inE(final Object v2Id);

    public abstract Traversal<Vertex, Edge> get_g_VX4X_bothE(final Object v4Id);

    public abstract Traversal<Vertex, Edge> get_g_VX4X_bothEXcreatedX(final Object v4Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_outE_inV(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX2X_inE_outV(final Object v2Id);

    public abstract Traversal<Vertex, Vertex> get_g_V_outE_hasXweight_1X_outV();

    public abstract Traversal<Vertex, String> get_g_V_out_outE_inV_inE_inV_both_name();

    public abstract Traversal<Vertex, String> get_g_VX1X_outEXknowsX_bothV_name(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_outXknowsX(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_outXknows_createdX(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_outEXknowsX_inV(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_outEXknows_createdX_inV(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_V_out_out();

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_out_out_out(final Object v1Id);

    public abstract Traversal<Vertex, String> get_g_VX1X_out_name(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_outE_otherV(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX4X_bothE_otherV(final Object v4Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX4X_bothE_hasXweight_lt_1X_otherV(final Object v4Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_to_XOUT_knowsX(final Object v1Id);

    // VERTEX ADJACENCY

    @Test
    @LoadGraphWith(MODERN)
    public void g_V() {
        final Traversal<Vertex, Vertex> traversal = get_g_V();
        printTraversalForm(traversal);
        int counter = 0;
        Set<Vertex> vertices = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            vertices.add(traversal.next());
        }
        assertEquals(6, vertices.size());
        assertEquals(6, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_out() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_out(convertToVertexId("marko"));
        assert_g_v1_out(traversal);
    }

    private void assert_g_v1_out(final Traversal<Vertex, Vertex> traversal) {
        printTraversalForm(traversal);
        int counter = 0;
        Set<Vertex> vertices = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            vertices.add(vertex);
            assertTrue(vertex.value("name").equals("vadas") ||
                    vertex.value("name").equals("josh") ||
                    vertex.value("name").equals("lop"));
        }
        assertEquals(3, counter);
        assertEquals(3, vertices.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX2X_in() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX2X_in(convertToVertexId("vadas"));
        assert_g_v2_in(traversal);
    }

    private void assert_g_v2_in(final Traversal<Vertex, Vertex> traversal) {
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            assertEquals(traversal.next().value("name"), "marko");
        }
        assertEquals(1, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX4X_both() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX4X_both(convertToVertexId("josh"));
        printTraversalForm(traversal);
        int counter = 0;
        Set<Vertex> vertices = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            vertices.add(vertex);
            assertTrue(vertex.value("name").equals("marko") ||
                    vertex.value("name").equals("ripple") ||
                    vertex.value("name").equals("lop"));
        }
        assertEquals(3, counter);
        assertEquals(3, vertices.size());
    }

    // EDGE ADJACENCY

    @Test
    @LoadGraphWith(MODERN)
    public void g_E() {
        final Traversal<Edge, Edge> traversal = get_g_E();
        printTraversalForm(traversal);
        int counter = 0;
        Set<Edge> edges = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            edges.add(traversal.next());
        }
        assertEquals(6, edges.size());
        assertEquals(6, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outE() {
        final Traversal<Vertex, Edge> traversal = get_g_VX1X_outE(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        Set<Edge> edges = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Edge edge = traversal.next();
            edges.add(edge);
            assertTrue(edge.label().equals("knows") || edge.label().equals("created"));
        }
        assertEquals(3, counter);
        assertEquals(3, edges.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX2X_inE() {
        final Traversal<Vertex, Edge> traversal = get_g_VX2X_inE(convertToVertexId("vadas"));
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            assertEquals(traversal.next().label(), "knows");
        }
        assertEquals(1, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX4X_bothEXcreatedX() {
        final Traversal<Vertex, Edge> traversal = get_g_VX4X_bothEXcreatedX(convertToVertexId("josh"));
        printTraversalForm(traversal);
        int counter = 0;
        Set<Edge> edges = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Edge edge = traversal.next();
            edges.add(edge);
            assertTrue(edge.label().equals("created"));
            assertEquals(edge.outV().id().next(), convertToVertexId("josh"));
        }
        assertEquals(2, counter);
        assertEquals(2, edges.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX4X_bothE() {
        final Traversal<Vertex, Edge> traversal = get_g_VX4X_bothE(convertToVertexId("josh"));
        printTraversalForm(traversal);
        int counter = 0;
        Set<Edge> edges = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Edge edge = traversal.next();
            edges.add(edge);
            assertTrue(edge.label().equals("knows") || edge.label().equals("created"));
        }
        assertEquals(3, counter);
        assertEquals(3, edges.size());
    }

    // EDGE/VERTEX ADJACENCY

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outE_inV() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_outE_inV(convertToVertexId("marko"));
        this.assert_g_v1_out(traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX2X_inE_outV() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX2X_inE_outV(convertToVertexId("vadas"));
        this.assert_g_v2_in(traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_outE_hasXweight_1X_outV() {
        final Traversal<Vertex, Vertex> traversal = get_g_V_outE_hasXweight_1X_outV();
        printTraversalForm(traversal);
        int counter = 0;
        Map<Object, Integer> counts = new HashMap<>();
        while (traversal.hasNext()) {
            final Object id = traversal.next().id();
            int previousCount = counts.getOrDefault(id, 0);
            counts.put(id, previousCount + 1);
            counter++;
        }
        assertEquals(2, counts.size());
        assertEquals(1, counts.get(convertToVertexId("marko")).intValue());
        assertEquals(1, counts.get(convertToVertexId("josh")).intValue());

        assertEquals(2, counter);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_out_outE_inV_inE_inV_both_name() {
        final Traversal<Vertex, String> traversal = get_g_V_out_outE_inV_inE_inV_both_name();
        printTraversalForm(traversal);
        int counter = 0;
        Map<String, Integer> counts = new HashMap<>();
        while (traversal.hasNext()) {
            final String key = traversal.next();
            int previousCount = counts.getOrDefault(key, 0);
            counts.put(key, previousCount + 1);
            counter++;
        }
        assertEquals(3, counts.size());
        assertEquals(4, counts.get("josh").intValue());
        assertEquals(3, counts.get("marko").intValue());
        assertEquals(3, counts.get("peter").intValue());

        assertEquals(10, counter);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outEXknowsX_bothV_name() {
        final Traversal<Vertex, String> traversal = get_g_VX1X_outEXknowsX_bothV_name(convertToVertexId("marko"));
        printTraversalForm(traversal);
        List<String> names = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(4, names.size());
        assertTrue(names.contains("marko"));
        assertTrue(names.contains("josh"));
        assertTrue(names.contains("vadas"));
        names.remove("marko");
        assertEquals(3, names.size());
        names.remove("marko");
        assertEquals(2, names.size());
        names.remove("josh");
        assertEquals(1, names.size());
        names.remove("vadas");
        assertEquals(0, names.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outE_otherV() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_outE_otherV(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        Set<Vertex> vertices = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            vertices.add(vertex);
            assertTrue(vertex.value("name").equals("vadas") ||
                    vertex.value("name").equals("josh") ||
                    vertex.value("name").equals("lop"));
        }
        assertEquals(3, counter);
        assertEquals(3, vertices.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX4X_bothE_otherV() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX4X_bothE_otherV(convertToVertexId("josh"));
        printTraversalForm(traversal);
        final List<Vertex> vertices = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(3, vertices.size());
        assertTrue(vertices.stream().anyMatch(v -> v.value("name").equals("marko")));
        assertTrue(vertices.stream().anyMatch(v -> v.value("name").equals("ripple")));
        assertTrue(vertices.stream().anyMatch(v -> v.value("name").equals("lop")));
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX4X_bothE_hasXweight_lt_1X_otherV() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX4X_bothE_hasXweight_lt_1X_otherV(convertToVertexId("josh"));
        printTraversalForm(traversal);
        final List<Vertex> vertices = StreamFactory.stream(traversal).collect(Collectors.toList());
        assertEquals(1, vertices.size());
        assertEquals(vertices.get(0).value("name"), "lop");
        assertFalse(traversal.hasNext());
    }

    // VERTEX EDGE LABEL ADJACENCY

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outXknowsX() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_outXknowsX(convertToVertexId("marko"));
        assert_g_v1_outXknowsX(traversal);
    }

    private void assert_g_v1_outXknowsX(Traversal<Vertex, Vertex> traversal) {
        printTraversalForm(traversal);
        int counter = 0;
        Set<Vertex> vertices = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            vertices.add(vertex);
            assertTrue(vertex.value("name").equals("vadas") ||
                    vertex.value("name").equals("josh"));
        }
        assertEquals(2, counter);
        assertEquals(2, vertices.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outXknows_createdX() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_outXknows_createdX(convertToVertexId("marko"));
        this.assert_g_v1_out(traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outEXknowsX_inV() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_outEXknowsX_inV(convertToVertexId("marko"));
        this.assert_g_v1_outXknowsX(traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_outEXknows_createdX_inV() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_outEXknows_createdX_inV(convertToVertexId("marko"));
        this.assert_g_v1_out(traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_out_out() {
        final Traversal<Vertex, Vertex> traversal = get_g_V_out_out();
        printTraversalForm(traversal);
        int counter = 0;
        Set<Vertex> vertices = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            vertices.add(vertex);
            assertTrue(vertex.value("name").equals("lop") ||
                    vertex.value("name").equals("ripple"));
        }
        assertEquals(2, counter);
        assertEquals(2, vertices.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_out_out_out() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_out_out_out(convertToVertexId("marko"));
        assertFalse(traversal.hasNext());
    }

    // PROPERTY TESTING

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_out_name() {
        final Traversal<Vertex, String> traversal = get_g_VX1X_out_name(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        Set<String> names = new HashSet<>();
        while (traversal.hasNext()) {
            counter++;
            String name = traversal.next();
            names.add(name);
            assertTrue(name.equals("vadas") ||
                    name.equals("josh") ||
                    name.equals("lop"));
        }
        assertEquals(3, counter);
        assertEquals(3, names.size());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_to_XOUT_knowsX() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_to_XOUT_knowsX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            String name = vertex.value("name");
            assertTrue(name.equals("vadas") ||
                    name.equals("josh"));
        }
        assertEquals(2, counter);
        assertFalse(traversal.hasNext());

    }

    public static class StandardTest extends VertexTest {
        public StandardTest() {
            requiresGraphComputer = false;
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V() {
            return g.V();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_out(final Object v1Id) {
            return g.V(v1Id).out();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX2X_in(final Object v2Id) {
            return g.V(v2Id).in();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX4X_both(final Object v4Id) {
            return g.V(v4Id).both();
        }

        @Override
        public Traversal<Edge, Edge> get_g_E() {
            return g.E();
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX1X_outE(final Object v1Id) {
            return g.V(v1Id).outE();
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX2X_inE(final Object v2Id) {
            return g.V(v2Id).inE();
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX4X_bothE(final Object v4Id) {
            return g.V(v4Id).bothE();
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX4X_bothEXcreatedX(final Object v4Id) {
            return g.V(v4Id).bothE("created");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outE_inV(final Object v1Id) {
            return g.V(v1Id).outE().inV();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX2X_inE_outV(final Object v2Id) {
            return g.V(v2Id).inE().outV();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_outE_hasXweight_1X_outV() {
            return g.V().outE().has("weight", 1.0d).outV();
        }

        @Override
        public Traversal<Vertex, String> get_g_V_out_outE_inV_inE_inV_both_name() {
            return g.V().out().outE().inV().inE().inV().both().values("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_outEXknowsX_bothV_name(final Object v1Id) {
            return g.V(v1Id).outE("knows").bothV().values("name");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outXknowsX(final Object v1Id) {
            return g.V(v1Id).out("knows");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outXknows_createdX(final Object v1Id) {
            return g.V(v1Id).out("knows", "created");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outEXknowsX_inV(final Object v1Id) {
            return g.V(v1Id).outE("knows").inV();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outEXknows_createdX_inV(final Object v1Id) {
            return g.V(v1Id).outE("knows", "created").inV();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outE_otherV(final Object v1Id) {
            return g.V(v1Id).outE().otherV();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX4X_bothE_otherV(final Object v4Id) {
            return g.V(v4Id).bothE().otherV();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX4X_bothE_hasXweight_lt_1X_otherV(Object v4Id) {
            return g.V(v4Id).bothE().has("weight", Compare.lt, 1d).otherV();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_out_out() {
            return g.V().out().out();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_out_out_out(final Object v1Id) {
            return g.V(v1Id).out().out().out();
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_out_name(final Object v1Id) {
            return g.V(v1Id).out().values("name");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_to_XOUT_knowsX(final Object v1Id) {
            return g.V(v1Id).to(Direction.OUT, "knows");
        }
    }

    public static class ComputerTest extends VertexTest {
        public ComputerTest() {
            requiresGraphComputer = true;
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V() {
            return g.V().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_out(final Object v1Id) {
            return g.V(v1Id).out().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX2X_in(final Object v2Id) {
            return g.V(v2Id).in().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX4X_both(final Object v4Id) {
            return g.V(v4Id).both().submit(g.compute());
        }

        @Override
        public Traversal<Edge, Edge> get_g_E() {
            return g.E().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX1X_outE(final Object v1Id) {
            return g.V(v1Id).outE().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX2X_inE(final Object v2Id) {
            return g.V(v2Id).inE().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX4X_bothE(final Object v4Id) {
            return g.V(v4Id).bothE().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Edge> get_g_VX4X_bothEXcreatedX(final Object v4Id) {
            return g.V(v4Id).bothE("created").submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outE_inV(final Object v1Id) {
            return g.V(v1Id).outE().inV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX2X_inE_outV(final Object v2Id) {
            return g.V(v2Id).inE().outV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_outE_hasXweight_1X_outV() {
            return g.V().outE().has("weight", 1.0d).outV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, String> get_g_V_out_outE_inV_inE_inV_both_name() {
            return g.V().out().outE().inV().inE().inV().both().<String>values("name").submit(g.compute());
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_outEXknowsX_bothV_name(final Object v1Id) {
            return g.V(v1Id).outE("knows").bothV().<String>values("name").submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outXknowsX(final Object v1Id) {
            return g.V(v1Id).out("knows").submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outXknows_createdX(final Object v1Id) {
            return g.V(v1Id).out("knows", "created").submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outEXknowsX_inV(final Object v1Id) {
            return g.V(v1Id).outE("knows").inV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outEXknows_createdX_inV(final Object v1Id) {
            return g.V(v1Id).outE("knows", "created").inV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_outE_otherV(final Object v1Id) {
            return g.V(v1Id).outE().otherV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX4X_bothE_otherV(final Object v4Id) {
            return g.V(v4Id).bothE().otherV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX4X_bothE_hasXweight_lt_1X_otherV(Object v4Id) {
            return g.V(v4Id).bothE().has("weight", Compare.lt, 1d).otherV().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_out_out() {
            return g.V().out().out().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_out_out_out(final Object v1Id) {
            return g.V(v1Id).out().out().out().submit(g.compute());
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_out_name(final Object v1Id) {
            return g.V(v1Id).out().<String>values("name").submit(g.compute());
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_to_XOUT_knowsX(final Object v1Id) {
            return g.V(v1Id).to(Direction.OUT, "knows").submit(g.compute());
        }
    }
}
