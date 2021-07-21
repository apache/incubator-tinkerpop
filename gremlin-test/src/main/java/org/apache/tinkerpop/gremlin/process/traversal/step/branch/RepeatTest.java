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
package org.apache.tinkerpop.gremlin.process.traversal.step.branch;

import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.GremlinProcessRunner;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.MapHelper;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization.RepeatUnrollStrategy;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.CREW;
import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.SINK;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.both;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.groupCount;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.loops;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.core.AnyOf.anyOf;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@RunWith(GremlinProcessRunner.class)
public abstract class RepeatTest extends AbstractGremlinProcessTest {

    // DO/WHILE


    public abstract Traversal<Vertex, Path> get_g_V_repeatXoutX_timesX2X_emit_path();

    public abstract Traversal<Vertex, String> get_g_V_repeatXoutX_timesX2X_repeatXinX_timesX2X_name();

    public abstract Traversal<Vertex, Vertex> get_g_V_repeatXoutX_timesX2X();

    public abstract Traversal<Vertex, Vertex> get_g_V_repeatXoutX_timesX2X_emit();

    public abstract Traversal<Vertex, Path> get_g_V_hasXloop_name_loopX_repeatXinX_timesX5X_path_by_name();

    // WHILE/DO

    public abstract Traversal<Vertex, String> get_g_VX1X_timesX2X_repeatXoutX_name(final Object v1Id);

    public abstract Traversal<Vertex, Path> get_g_V_emit_timesX2X_repeatXoutX_path();

    public abstract Traversal<Vertex, Path> get_g_V_emit_repeatXoutX_timesX2X_path();

    public abstract Traversal<Vertex, String> get_g_VX1X_emitXhasXlabel_personXX_repeatXoutX_name(final Object v1Id);

    // SIDE-EFFECTS

    public abstract Traversal<Vertex, Map<String, Long>> get_g_V_repeatXgroupCountXmX_byXnameX_outX_timesX2X_capXmX();

    public abstract Traversal<Vertex, Map<Integer, Long>> get_g_VX1X_repeatXgroupCountXmX_byXloopsX_outX_timesX3X_capXmX(final Object v1Id);

    //

    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_repeatXbothX_timesX10X_asXaX_out_asXbX_selectXa_bX();

    public abstract Traversal<Vertex, String> get_g_VX1X_repeatXoutX_untilXoutE_count_isX0XX_name(final Object v1Id);

    public abstract Traversal<Vertex, Map<String, Long>> get_g_V_repeatXbothX_untilXname_eq_marko_or_loops_gt_1X_groupCount_byXnameX();

    public abstract Traversal<Vertex, Path> get_g_V_hasXname_markoX_repeatXoutE_inV_simplePathX_untilXhasXname_rippleXX_path_byXnameX_byXlabelX();

    // NESTED LOOP

    public abstract Traversal<Vertex, Path> get_g_V_repeatXout_repeatXoutX_timesX1XX_timesX1X_limitX1X_path_by_name();

    public abstract Traversal<Vertex, Path> get_g_V_repeatXoutXknowsXX_untilXrepeatXoutXcreatedXX_emitXhasXname_lopXXX_path_byXnameX();

    public abstract Traversal<Vertex, String> get_g_V_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang();

    public abstract Traversal<Vertex, String> get_g_V_untilXconstantXtrueXX_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang();

    public abstract Traversal<Vertex, String> get_g_VX3X_repeatXbothX_createdXX_untilXloops_is_40XXemit_repeatXin_knowsXX_emit_loopsXisX1Xdedup_values(final Object v3Id);

    public abstract Traversal<Vertex, String> get_g_VX1X_repeatXrepeatXunionXout_uses_out_traversesXX_whereXloops_isX0X_timesX1X_timeX2X_name(final Object v1Id);

    // NAMED LOOP

    public abstract Traversal<Vertex, String> get_g_V_repeatXa_outXknows_repeatXb_outXcreatedX_filterXloops_isX0XX_emit_lang();

    public abstract Traversal<Vertex, String> get_g_V_emit_repeatXa_outXknows_filterXloops_isX0XX_lang();

    public abstract Traversal<Vertex, String> get_g_VX6X_repeatXa_bothXcreatedX_simplePathX_emitXrepeatXb_bothXknowsXX_untilXloopsXbX_asXb_whereXloopsXaX_asXbX_hasXname_vadasXX_dedup_name(final Object v6Id);

    public abstract Traversal<Vertex, Vertex> get_g_V_hasXname_markoX_repeatXoutE_order_byXweight_decrX_inVX_emit();

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXoutX_timesX2X_emit_path() {
        final List<Traversal<Vertex, Path>> traversals = new ArrayList<>();
        traversals.add(get_g_V_repeatXoutX_timesX2X_emit_path());
        traversals.forEach(traversal -> {
            printTraversalForm(traversal);
            final Map<Integer, Long> pathLengths = new HashMap<>();
            int counter = 0;
            while (traversal.hasNext()) {
                counter++;
                MapHelper.incr(pathLengths, traversal.next().size(), 1l);
            }
            assertEquals(2, pathLengths.size());
            assertEquals(8, counter);
            assertEquals(new Long(6), pathLengths.get(2));
            assertEquals(new Long(2), pathLengths.get(3));
        });
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXoutX_timesX2X_repeatXinX_timesX2X_name() {
        final Traversal<Vertex, String> traversal = get_g_V_repeatXoutX_timesX2X_repeatXinX_timesX2X_name();
        printTraversalForm(traversal);
        checkResults(Arrays.asList("marko", "marko"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXoutX_timesX2X() {
        final Traversal<Vertex, Vertex> traversal = get_g_V_repeatXoutX_timesX2X();
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            counter++;
            Vertex vertex = traversal.next();
            assertTrue(vertex.value("name").equals("lop") || vertex.value("name").equals("ripple"));
        }
        assertEquals(2, counter);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXoutX_timesX2X_emit() {
        final Traversal<Vertex, Vertex> traversal = get_g_V_repeatXoutX_timesX2X_emit();
        printTraversalForm(traversal);
        final Map<String, Long> map = new HashMap<>();
        while (traversal.hasNext()) {
            final Vertex vertex = traversal.next();
            MapHelper.incr(map, vertex.value("name"), 1l);
        }
        assertEquals(4, map.size());
        assertTrue(map.containsKey("vadas"));
        assertTrue(map.containsKey("josh"));
        assertTrue(map.containsKey("ripple"));
        assertTrue(map.containsKey("lop"));
        assertEquals(new Long(1), map.get("vadas"));
        assertEquals(new Long(1), map.get("josh"));
        assertEquals(new Long(2), map.get("ripple"));
        assertEquals(new Long(4), map.get("lop"));
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_timesX2X_repeatXoutX_name() {
        final Traversal<Vertex, String> traversal = get_g_VX1X_timesX2X_repeatXoutX_name(convertToVertexId("marko"));
        printTraversalForm(traversal);
        checkResults(Arrays.asList("lop", "ripple"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_emit_timesX2X_repeatXoutX_path() {
        final Traversal<Vertex, Path> traversal = get_g_V_emit_timesX2X_repeatXoutX_path();
        printTraversalForm(traversal);
        assertPath(traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_emit_repeatXoutX_timesX2X_path() {
        final Traversal<Vertex, Path> traversal = get_g_V_emit_repeatXoutX_timesX2X_path();
        printTraversalForm(traversal);
        assertPath(traversal);
    }

    private static void assertPath(final Traversal<Vertex, Path> traversal) {
        int path1 = 0;
        int path2 = 0;
        int path3 = 0;
        while (traversal.hasNext()) {
            final Path path = traversal.next();
            if (path.size() == 1) {
                path1++;
            } else if (path.size() == 2) {
                path2++;
            } else if (path.size() == 3) {
                path3++;
            } else {
                fail("Only path lengths of 1, 2, or 3 should be seen");
            }
        }
        assertEquals(6, path1);
        assertEquals(6, path2);
        assertEquals(2, path3);
    }

    @Test
    @LoadGraphWith(SINK)
    public void g_V_hasXloop_name_loopX_repeatXinX_timesX5X_path_by_name() {
        final Traversal<Vertex, Path> traversal = get_g_V_hasXloop_name_loopX_repeatXinX_timesX5X_path_by_name();
        printTraversalForm(traversal);
        final Path path = traversal.next();
        assertThat(path, contains("loop", "loop", "loop", "loop", "loop", "loop"));
        assertThat(traversal.hasNext(), is(false));
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_emitXhasXlabel_personXX_repeatXoutX_name() {
        final Traversal<Vertex, String> traversal = get_g_VX1X_emitXhasXlabel_personXX_repeatXoutX_name(convertToVertexId("marko"));
        printTraversalForm(traversal);
        checkResults(Arrays.asList("marko", "josh", "vadas"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXgroupCountXmX_byXnameX_outX_timesX2X_capXmX() {
        final Traversal<Vertex, Map<String, Long>> traversal = get_g_V_repeatXgroupCountXmX_byXnameX_outX_timesX2X_capXmX();
        printTraversalForm(traversal);
        final Map<String, Long> map = traversal.next();
        assertFalse(traversal.hasNext());
        //[ripple:2, peter:1, vadas:2, josh:2, lop:4, marko:1]
        assertEquals(6, map.size());
        assertEquals(1l, map.get("marko").longValue());
        assertEquals(2l, map.get("vadas").longValue());
        assertEquals(2l, map.get("josh").longValue());
        assertEquals(4l, map.get("lop").longValue());
        assertEquals(2l, map.get("ripple").longValue());
        assertEquals(1l, map.get("peter").longValue());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXbothX_timesX10X_asXaX_out_asXbX_selectXa_bX() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_repeatXbothX_timesX10X_asXaX_out_asXbX_selectXa_bX();
        printTraversalForm(traversal);
        int counter = 0;
        while (traversal.hasNext()) {
            final Map<String, Vertex> map = traversal.next();
            assertEquals(2, map.size());
            assertTrue(map.get("a") instanceof Vertex);
            assertTrue(map.get("b") instanceof Vertex);
            counter++;
        }
        assertTrue(counter > 0);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_repeatXoutX_untilXoutE_count_isX0XX_name() {
        final Traversal<Vertex, String> traversal = get_g_VX1X_repeatXoutX_untilXoutE_count_isX0XX_name(convertToVertexId("marko"));
        printTraversalForm(traversal);
        checkResults(Arrays.asList("lop", "lop", "ripple", "vadas"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_repeatXgroupCountXmX_byXloopsX_outX_timesX3X_capXmX() {
        final Traversal<Vertex, Map<Integer, Long>> traversal = get_g_VX1X_repeatXgroupCountXmX_byXloopsX_outX_timesX3X_capXmX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        final Map<Integer, Long> map = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(3, map.size());
        // {0=1, 1=3, 2=2}
        assertEquals(1L, map.get(0).longValue());
        assertEquals(3L, map.get(1).longValue());
        assertEquals(2L, map.get(2).longValue());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXbothX_untilXname_eq_marko_or_loops_gt_1X_groupCount_byXnameX() {
        final Traversal<Vertex, Map<String, Long>> traversal = get_g_V_repeatXbothX_untilXname_eq_marko_or_loops_gt_1X_groupCount_byXnameX();
        printTraversalForm(traversal);
        final Map<String, Long> map = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(5, map.size());
        assertEquals(3L, map.get("ripple").longValue());
        assertEquals(3L, map.get("vadas").longValue());
        assertEquals(4L, map.get("josh").longValue());
        assertEquals(10L, map.get("lop").longValue());
        assertEquals(4L, map.get("marko").longValue());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_hasXname_markoX_repeatXoutE_inV_simplePathX_untilXhasXname_rippleXX_path_byXnameX_byXlabelX() {
        final Traversal<Vertex, Path> traversal = get_g_V_hasXname_markoX_repeatXoutE_inV_simplePathX_untilXhasXname_rippleXX_path_byXnameX_byXlabelX();
        printTraversalForm(traversal);
        final Path path = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(5, path.size());
        assertEquals("marko", path.get(0));
        assertEquals("knows", path.get(1));
        assertEquals("josh", path.get(2));
        assertEquals("created", path.get(3));
        assertEquals("ripple", path.get(4));
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXout_repeatXoutX_timesX1XX_timesX1X_limitX1X_path_by_name() {
        // This traversal gets optimised by the RepeatUnrollStrategy
        final Traversal<Vertex, Path> traversal_unrolled = get_g_V_repeatXout_repeatXoutX_timesX1XX_timesX1X_limitX1X_path_by_name();
        final Path pathOriginal = traversal_unrolled.next();
        assertFalse(traversal_unrolled.hasNext());
        assertEquals(3, pathOriginal.size());
        assertEquals("marko", pathOriginal.get(0));
        assertEquals("josh", pathOriginal.get(1));

        // could be lop or ripple depending on what the graph chooses to traverse first
        assertThat(pathOriginal.get(2), anyOf(equalTo("ripple"), equalTo("lop")));

        g = g.withoutStrategies(RepeatUnrollStrategy.class);

        final Traversal<Vertex, Path> traversal = get_g_V_repeatXout_repeatXoutX_timesX1XX_timesX1X_limitX1X_path_by_name();
        printTraversalForm(traversal);
        final Path path = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(3, path.size());
        assertEquals("marko", path.get(0));
        assertEquals("josh", path.get(1));

        // could be lop or ripple depending on what the graph chooses to traverse first
        assertThat(path.get(2), anyOf(equalTo("ripple"), equalTo("lop")));
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXoutXknowsXX_untilXrepeatXoutXcreatedXX_emitXhasXname_lopXXX_path_byXnameX() {
        final Traversal<Vertex, Path> traversal = get_g_V_repeatXoutXknowsXX_untilXrepeatXoutXcreatedXX_emitXhasXname_lopXXX_path_byXnameX();
        printTraversalForm(traversal);
        final Path path = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(2, path.size());
        assertEquals("marko", path.get(0));
        assertEquals("josh", path.get(1));
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang() {
        final Traversal<Vertex, String> traversal = get_g_V_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang();
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        String lang = traversal.next();
        assertEquals(lang, "java");
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_untilXconstantXtrueXX_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang() {
        final Traversal<Vertex, String> traversal = get_g_V_untilXconstantXtrueXX_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang();
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        String lang = traversal.next();
        assertEquals(lang, "java");
        assertTrue(traversal.hasNext());
        lang = traversal.next();
        assertEquals(lang, "java");
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX3X_repeatXbothX_createdXX_untilXloops_is_40XXemit_repeatXin_knowsXX_emit_loopsXisX1Xdedup_values() {
        final Traversal<Vertex, String> traversal = get_g_VX3X_repeatXbothX_createdXX_untilXloops_is_40XXemit_repeatXin_knowsXX_emit_loopsXisX1Xdedup_values(convertToVertexId("lop"));
        printTraversalForm(traversal);
        checkResults(Arrays.asList("josh", "ripple", "lop"), traversal);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(CREW)
    public void g_VX1X_repeatXrepeatXunionXout_uses_out_traversesXX_whereXloops_isX0X_timesX1X_timeX2X_name() {
        final Traversal<Vertex, String> traversal = get_g_VX1X_repeatXrepeatXunionXout_uses_out_traversesXX_whereXloops_isX0X_timesX1X_timeX2X_name(convertToVertexId("marko"));
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        String name = traversal.next();
        assertEquals(name, "tinkergraph");
        assertFalse(traversal.hasNext());
    }

    @LoadGraphWith(MODERN)
    public void g_V_repeatXa_outXknows_repeatXb_outXcreatedX_filterXloops_isX0XX_emit_lang() {
        final Traversal<Vertex, String> traversal = get_g_V_repeatXa_outXknows_repeatXb_outXcreatedX_filterXloops_isX0XX_emit_lang();
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        String lang = traversal.next();
        assertEquals(lang, "java");
        assertTrue(traversal.hasNext());
        lang = traversal.next();
        assertEquals(lang, "java");
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_emit_repeatXa_outXknows_filterXloops_isX0XX_lang() {
        final Traversal<Vertex, String> traversal = get_g_V_emit_repeatXa_outXknows_filterXloops_isX0XX_lang();
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        String lang = traversal.next();
        assertEquals(lang, "java");
        assertTrue(traversal.hasNext());
        lang = traversal.next();
        assertEquals(lang, "java");
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX6X_repeatXa_bothXcreatedX_simplePathX_emitXrepeatXb_bothXknowsXX_untilXloopsXbX_asXb_whereXloopsXaX_asXbX_hasXname_vadasXX_dedup_name() {
        final Traversal<Vertex, String> traversal = get_g_VX6X_repeatXa_bothXcreatedX_simplePathX_emitXrepeatXb_bothXknowsXX_untilXloopsXbX_asXb_whereXloopsXaX_asXbX_hasXname_vadasXX_dedup_name(convertToVertexId("peter"));
        printTraversalForm(traversal);
        assertTrue(traversal.hasNext());
        String name = traversal.next();
        assertEquals(name, "josh");
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_hasXname_markoX_repeatXoutE_order_byXweight_decrX_inVX_emit() {
        final List<Vertex> vertices = get_g_V_hasXname_markoX_repeatXoutE_order_byXweight_decrX_inVX_emit().toList();
        assertEquals(5, vertices.size());
        assertEquals("josh", vertices.get(0).values("name").next());
        assertEquals("ripple", vertices.get(1).values("name").next());
        assertEquals("lop", vertices.get(2).values("name").next());
        assertEquals("vadas", vertices.get(3).values("name").next());
        assertEquals("lop", vertices.get(4).values("name").next());
    }

    public static class Traversals extends RepeatTest {

        @Override
        public Traversal<Vertex, Path> get_g_V_repeatXoutX_timesX2X_emit_path() {
            return g.V().repeat(out()).times(2).emit().path();
        }

        @Override
        public Traversal<Vertex, String> get_g_V_repeatXoutX_timesX2X_repeatXinX_timesX2X_name() {
            return g.V().repeat(out()).times(2).repeat(in()).times(2).values("name");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_repeatXoutX_timesX2X() {
            return g.V().repeat(out()).times(2);
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_repeatXoutX_timesX2X_emit() {
            return g.V().repeat(out()).times(2).emit();
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_timesX2X_repeatXoutX_name(final Object v1Id) {
            return g.V(v1Id).times(2).repeat(out()).values("name");
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_emit_repeatXoutX_timesX2X_path() {
            return g.V().emit().repeat(out()).times(2).path();
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_emit_timesX2X_repeatXoutX_path() {
            return g.V().emit().times(2).repeat(out()).path();
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_emitXhasXlabel_personXX_repeatXoutX_name(final Object v1Id) {
            return g.V(v1Id).emit(has(T.label, "person")).repeat(out()).values("name");
        }

        @Override
        public Traversal<Vertex, Map<String, Long>> get_g_V_repeatXgroupCountXmX_byXnameX_outX_timesX2X_capXmX() {
            return g.V().repeat(groupCount("m").by("name").out()).times(2).cap("m");
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_repeatXbothX_timesX10X_asXaX_out_asXbX_selectXa_bX() {
            return g.V().repeat(both()).times(10).as("a").out().as("b").select("a", "b");
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_repeatXoutX_untilXoutE_count_isX0XX_name(final Object v1Id) {
            return g.V(v1Id).repeat(out()).until(outE().count().is(0)).values("name");
        }

        @Override
        public Traversal<Vertex, Map<Integer, Long>> get_g_VX1X_repeatXgroupCountXmX_byXloopsX_outX_timesX3X_capXmX(final Object v1Id) {
            return g.V(v1Id).repeat(groupCount("m").by(loops()).out()).times(3).cap("m");
        }

        @Override
        public Traversal<Vertex, Map<String, Long>> get_g_V_repeatXbothX_untilXname_eq_marko_or_loops_gt_1X_groupCount_byXnameX() {
            return g.V().repeat(both()).until(t -> t.get().value("name").equals("lop") || t.loops() > 1).<String>groupCount().by("name");
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_hasXname_markoX_repeatXoutE_inV_simplePathX_untilXhasXname_rippleXX_path_byXnameX_byXlabelX() {
            return g.V().has("name", "marko").repeat(outE().inV().simplePath()).until(has("name", "ripple")).path().by("name").by(T.label);
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_hasXloop_name_loopX_repeatXinX_timesX5X_path_by_name() {
            return g.V().has("loops","name","loop").repeat(__.in()).times(5).path().by("name");
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_repeatXout_repeatXoutX_timesX1XX_timesX1X_limitX1X_path_by_name() {
            // NB We need to prevent the RepeatUnrollStrategy from applying to properly exercise this test as this traversal can be simplified
            return g.V().repeat(out().repeat(out()).times(1)).times(1).limit(1).path().by("name");
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_repeatXoutXknowsXX_untilXrepeatXoutXcreatedXX_emitXhasXname_lopXXX_path_byXnameX() {
            return g.V().repeat(out("knows")).until(__.repeat(out("created")).emit(__.has("name", "lop"))).path().by("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang() {
            return g.V().repeat(__.repeat(out("created")).until(__.has("name", "ripple"))).emit().values("lang");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_untilXconstantXtrueXX_repeatXrepeatXout_createdXX_untilXhasXname_rippleXXXemit_lang() {
            return g.V().until(__.constant(true)).repeat(__.repeat(out("created")).until(__.has("name", "ripple"))).emit().values("lang");
        }

        @Override
        public Traversal<Vertex, String> get_g_VX3X_repeatXbothX_createdXX_untilXloops_is_40XXemit_repeatXin_knowsXX_emit_loopsXisX1Xdedup_values(final Object v3Id) {
            return g.V(v3Id).repeat(__.both("created")).until(loops().is(40)).emit(__.repeat(__.in("knows")).emit(loops().is(1))).dedup().values("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_repeatXa_outXknows_repeatXb_outXcreatedX_filterXloops_isX0XX_emit_lang() {
            return g.V().repeat("a", out("knows").repeat("b", out("created").filter(loops("a").is(0))).emit()).emit().values("lang");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_emit_repeatXa_outXknows_filterXloops_isX0XX_lang() {
            return g.V().emit().repeat("a", out("knows").filter(loops("a").is(0))).values("lang");
        }

        @Override
        public Traversal<Vertex, String> get_g_VX6X_repeatXa_bothXcreatedX_simplePathX_emitXrepeatXb_bothXknowsXX_untilXloopsXbX_asXb_whereXloopsXaX_asXbX_hasXname_vadasXX_dedup_name(final Object v6Id) {
            return g.V(v6Id).repeat("a", both("created").simplePath()).emit(__.repeat("b", __.both("knows")).until(loops("b").as("b").where(loops("a").as("b"))).has("name", "vadas")).dedup().values("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_repeatXrepeatXunionXout_uses_out_traversesXX_whereXloops_isX0X_timesX1X_timeX2X_name(final Object v1Id) {
            return g.V(v1Id).repeat(__.repeat(__.union(out("uses"), out("traverses")).where(__.loops().is(0))).times(1)).times(2).values("name");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_hasXname_markoX_repeatXoutE_order_byXweight_decrX_inVX_emit() {
            return g.V().has("name", "marko").repeat(__.outE().order().by("weight", decr).inV()).emit();
        }
    }
}
