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
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.map;

import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.graph.traversal.__;
import org.apache.tinkerpop.gremlin.process.traversal.engine.StandardTraversalEngine;
import org.apache.tinkerpop.gremlin.structure.Order;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.*;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.CREW;
import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.apache.tinkerpop.gremlin.process.graph.traversal.__.values;
import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class SelectTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_VX1X_asXaX_outXknowsX_asXbX_select(final Object v1Id);

    public abstract Traversal<Vertex, Map<String, String>> get_g_VX1X_asXaX_outXknowsX_asXbX_select_byXnameX(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX(final Object v1Id);

    public abstract Traversal<Vertex, String> get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX_byXnameX(final Object v1Id);

    public abstract Traversal<Vertex, Map<String, String>> get_g_V_asXaX_out_asXbX_select_byXnameX();

    public abstract Traversal<Vertex, Map<String, String>> get_g_V_asXaX_out_aggregate_asXbX_select_byXnameX();

    public abstract Traversal<Vertex, Map<String, String>> get_g_V_asXaX_name_order_asXbX_select_byXnameX_by();

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_hasXname_gremlinX_inEXusesX_order_byXskill_incrX_asXaX_outV_asXbX_select_byXskillX_byXnameX();

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_hasXname_isXmarkoXX_asXaX_select();

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_label_groupCount_cap_asXxX_select();

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_hasLabelXpersonX_asXpersonX_localXbothE_label_groupCount_capX_asXrelationsX_select_byXnameX_by();

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_asXaX_outXknowsX_asXbX_select() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_VX1X_asXaX_outXknowsX_asXbX_select(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            Map<String, Vertex> bindings = traversal.next();
            assertEquals(2, bindings.size());
            assertEquals(convertToVertexId("marko"), (bindings.get("a")).id());
            assertTrue((bindings.get("b")).id().equals(convertToVertexId("vadas")) || bindings.get("b").id().equals(convertToVertexId("josh")));
        }
        assertEquals(2, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_asXaX_outXknowsX_asXbX_select_byXnameX() {
        final Traversal<Vertex, Map<String, String>> traversal = get_g_VX1X_asXaX_outXknowsX_asXbX_select_byXnameX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            Map<String, String> bindings = traversal.next();
            assertEquals(2, bindings.size());
            assertEquals("marko", bindings.get("a"));
            assertTrue(bindings.get("b").equals("josh") || bindings.get("b").equals("vadas"));
        }
        assertEquals(2, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_asXaX_outXknowsX_asXbX_selectXaX() {
        final Traversal<Vertex, Vertex> traversal = get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            assertEquals(convertToVertexId("marko"), vertex.id());
        }
        assertEquals(2, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_asXaX_outXknowsX_asXbX_selectXaX_byXnameX() {
        final Traversal<Vertex, String> traversal = get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX_byXnameX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            assertEquals("marko", traversal.next());
        }
        assertEquals(2, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_asXaX_out_asXbX_select_byXnameX() {
        Arrays.asList(
                get_g_V_asXaX_out_asXbX_select_byXnameX(),
                get_g_V_asXaX_out_aggregate_asXbX_select_byXnameX()).forEach(traversal -> {
            printTraversalForm(traversal);
            final List<Map<String, String>> expected = makeMapList(2,
                    "a", "marko", "b", "lop",
                    "a", "marko", "b", "vadas",
                    "a", "marko", "b", "josh",
                    "a", "josh", "b", "ripple",
                    "a", "josh", "b", "lop",
                    "a", "peter", "b", "lop");
            checkResults(expected, traversal);
        });
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_asXaX_name_order_asXbX_select_byXnameX_byXitX() {
        Arrays.asList(
                get_g_V_asXaX_name_order_asXbX_select_byXnameX_by()).forEach(traversal -> {
            printTraversalForm(traversal);
            final List<Map<String, String>> expected = makeMapList(2,
                    "a", "marko", "b", "marko",
                    "a", "vadas", "b", "vadas",
                    "a", "josh", "b", "josh",
                    "a", "ripple", "b", "ripple",
                    "a", "lop", "b", "lop",
                    "a", "peter", "b", "peter");
            checkResults(expected, traversal);
        });
    }

    @Test
    @LoadGraphWith(CREW)
    public void g_V_hasXname_gremlinX_inEXusesX_order_byXskill_incrX_asXaX_outV_asXbX_select_byXskillX_byXnameX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_hasXname_gremlinX_inEXusesX_order_byXskill_incrX_asXaX_outV_asXbX_select_byXskillX_byXnameX();
        printTraversalForm(traversal);
        final List<Map<String, Object>> expected = makeMapList(2,
                "a", 3, "b", "matthias",
                "a", 4, "b", "marko",
                "a", 5, "b", "stephen",
                "a", 5, "b", "daniel");
        checkResults(expected, traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_hasXname_isXmarkoXX_asXaX_select() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_hasXname_isXmarkoXX_asXaX_select();
        printTraversalForm(traversal);
        final List<Map<String, Object>> expected = makeMapList(1, "a", g.V(convertToVertexId("marko")).next());
        checkResults(expected, traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_label_groupCount_cap_asXxX_select() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_label_groupCount_cap_asXxX_select();
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        final Map<String, Object> map1 = traversal.next();
        assertEquals(1, map1.size());
        assertTrue(map1.containsKey("x"));
        final Map<String, Long> map2 = (Map<String, Long>) map1.get("x");
        assertEquals(2, map2.size());
        assertTrue(map2.containsKey("person"));
        assertTrue(map2.containsKey("software"));
        assertEquals(2, map2.get("software").longValue());
        assertEquals(4, map2.get("person").longValue());
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_hasLabelXpersonX_asXpersonX_localXbothE_label_groupCount_capX_asXrelationsX_select_byXnameX_by() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_hasLabelXpersonX_asXpersonX_localXbothE_label_groupCount_capX_asXrelationsX_select_byXnameX_by();
        printTraversalForm(traversal);
        final Set<String> persons = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            assertTrue(traversal.hasNext());
            final Map<String, Object> map = traversal.next();
            assertEquals(2, map.size());
            assertTrue(map.containsKey("person"));
            assertTrue(map.containsKey("relations"));
            assertTrue(persons.add((String) map.get("person")));
            final Map<String, Long> relations = (Map<String, Long>) map.get("relations");
            switch ((String) map.get("person")) {
                case "marko":
                    assertEquals(2, relations.size());
                    assertEquals(1, relations.get("created").longValue());
                    assertEquals(2, relations.get("knows").longValue());
                    break;
                case "vadas":
                    assertEquals(1, relations.size());
                    assertEquals(1, relations.get("knows").longValue());
                    break;
                case "josh":
                    assertEquals(2, relations.size());
                    assertEquals(2, relations.get("created").longValue());
                    assertEquals(1, relations.get("knows").longValue());
                    break;
                case "peter":
                    assertEquals(1, relations.size());
                    assertEquals(1, relations.get("created").longValue());
                    break;
                default:
                    assertTrue(false);
                    break;
            }
        }
        assertFalse(traversal.hasNext());
        assertEquals(4, persons.size());
    }

    public static class StandardTest extends SelectTest {
        public StandardTest() {
            requiresGraphComputer = false;
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_VX1X_asXaX_outXknowsX_asXbX_select(final Object v1Id) {
            return g.V(v1Id).as("a").out("knows").as("b").select();
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_VX1X_asXaX_outXknowsX_asXbX_select_byXnameX(final Object v1Id) {
            return g.V(v1Id).as("a").out("knows").as("b").<String>select().by("name");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX(final Object v1Id) {
            return g.V(v1Id).as("a").out("knows").as("b").select("a");
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX_byXnameX(final Object v1Id) {
            return g.V(v1Id).as("a").out("knows").as("b").<String>select("a").by("name");
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_asXaX_out_asXbX_select_byXnameX() {
            return g.V().as("a").out().as("b").<String>select().by("name");
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_asXaX_out_aggregate_asXbX_select_byXnameX() {
            return g.V().as("a").out().aggregate().as("b").<String>select().by("name");
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_asXaX_name_order_asXbX_select_byXnameX_by() {
            return g.V().as("a").values("name").order().as("b").<String>select().by("name").by();
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_hasXname_gremlinX_inEXusesX_order_byXskill_incrX_asXaX_outV_asXbX_select_byXskillX_byXnameX() {
            return g.V().has("name", "gremlin").inE("uses").order().by("skill", Order.incr).as("a").outV().as("b").select().by("skill").by("name");
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_hasXname_isXmarkoXX_asXaX_select() {
            return g.V().has(values("name").is("marko")).as("a").select();
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_label_groupCount_cap_asXxX_select() {
            return g.V().label().groupCount().cap().as("x").select();
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_hasLabelXpersonX_asXpersonX_localXbothE_label_groupCount_capX_asXrelationsX_select_byXnameX_by() {
            return g.V().hasLabel("person").as("person").local(__.bothE().label().groupCount()).as("relations").select().by("name").by();
        }
    }

    public static class ComputerTest extends SelectTest {
        public ComputerTest() {
            requiresGraphComputer = true;
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_VX1X_asXaX_outXknowsX_asXbX_select(final Object v1Id) {
            return g.V(v1Id).as("a").out("knows").as("b").<Vertex>select();
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_VX1X_asXaX_outXknowsX_asXbX_select_byXnameX(final Object v1Id) {
            // TODO: Micro elements do not store properties
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V(v1Id).as("a").out("knows").as("b").<String>select().by("name"); //;
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX(final Object v1Id) {
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V(v1Id).as("a").out("knows").as("b").<Vertex>select("a");  // TODO
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_asXaX_outXknowsX_asXbX_selectXaX_byXnameX(final Object v1Id) {
            // TODO: Micro elements do not store properties
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V(v1Id).as("a").out("knows").as("b").<String>select("a").by("name");  // ;
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_asXaX_out_asXbX_select_byXnameX() {
            // TODO: Micro elements do not store properties
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V().as("a").out().as("b").<String>select().by("name");  // ;
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_asXaX_out_aggregate_asXbX_select_byXnameX() {
            // TODO: Micro elements do not store properties
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V().as("a").out().aggregate().as("b").<String>select().by("name");
        }

        @Override
        public Traversal<Vertex, Map<String, String>> get_g_V_asXaX_name_order_asXbX_select_byXnameX_by() {
            // TODO: Micro elements do not store properties
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V().as("a").values("name").order().as("b").<String>select().by("name").by();
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_hasXname_gremlinX_inEXusesX_order_byXskill_incrX_asXaX_outV_asXbX_select_byXskillX_byXnameX() {
            // TODO: Micro elements do not store properties
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V().has("name", "gremlin").inE("uses").order().by("skill", Order.incr).as("a").outV().as("b").select().by("skill").by("name");
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_hasXname_isXmarkoXX_asXaX_select() {
            return g.V().has(values("name").is("marko")).as("a").select();
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_label_groupCount_cap_asXxX_select() {
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V().label().groupCount().cap().as("x").select();
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_hasLabelXpersonX_asXpersonX_localXbothE_label_groupCount_capX_asXrelationsX_select_byXnameX_by() {
            g.engine(StandardTraversalEngine.standard); // TODO
            return g.V().hasLabel("person").as("person").local(__.bothE().label().groupCount()).as("relations").select().by("name").by();
        }
    }
}
