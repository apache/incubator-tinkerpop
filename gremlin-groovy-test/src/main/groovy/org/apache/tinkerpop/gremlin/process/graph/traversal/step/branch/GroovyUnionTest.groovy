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
package com.tinkerpop.gremlin.process.graph.traversal.step.branch

import com.tinkerpop.gremlin.process.ComputerTestHelper
import com.tinkerpop.gremlin.process.Traversal
import com.tinkerpop.gremlin.process.graph.traversal.__
import com.tinkerpop.gremlin.process.graph.traversal.step.branch.UnionTest
import com.tinkerpop.gremlin.structure.Vertex

import static com.tinkerpop.gremlin.process.graph.traversal.__.*

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GroovyUnionTest {

    public static class StandardTest extends UnionTest {

        public Traversal<Vertex, String> get_g_V_unionXout__inX_name() {
            g.V.union(__.out, __.in).name
        }

        public Traversal<Vertex, String> get_g_VX1X_unionXrepeatXoutX_timesX2X__outX_name(final Object v1Id) {
            g.V(v1Id).union(repeat(__.out).times(2), __.out).name
        }

        public Traversal<Vertex, String> get_g_V_chooseXlabel_is_person__unionX__out_lang__out_nameX__in_labelX() {
            g.V.choose(__.label.is('person'), union(__.out.lang, __.out.name), __.in.label)
        }

        public Traversal<Vertex, Map<String, Long>> get_g_V_chooseXlabel_is_person__unionX__out_lang__out_nameX__in_labelX_groupCount() {
            g.V.choose(__.label.is('person'), union(__.out.lang, __.out.name), __.in.label).groupCount
        }

        public Traversal<Vertex, Map<String, Long>> get_g_V_unionXrepeatXunionXoutXcreatedX__inXcreatedXX_timesX2X__repeatXunionXinXcreatedX__outXcreatedXX_timesX2XX_label_groupCount() {
            g.V.union(
                    repeat(union(
                            out('created'),
                            __.in('created'))).times(2),
                    repeat(union(
                            __.in('created'),
                            out('created'))).times(2)).label.groupCount()
        }

        @Override
        public Traversal<Vertex, Number> get_g_VX1_2X_unionXoutE_count__inE_count__outE_weight_sumX(
                final Object v1Id, final Object v2Id) {
            g.V(v1Id, v2Id).union(outE().count, inE().count, outE().weight.sum);
        }

    }

    public static class ComputerTest extends UnionTest {

        public Traversal<Vertex, String> get_g_V_unionXout__inX_name() {
            ComputerTestHelper.compute("g.V.union(__.out, __.in).name", g)
        }

        public Traversal<Vertex, String> get_g_VX1X_unionXrepeatXoutX_timesX2X__outX_name(final Object v1Id) {
            ComputerTestHelper.compute("g.V(${v1Id}).union(repeat(__.out).times(2), __.out).name", g)
        }

        public Traversal<Vertex, String> get_g_V_chooseXlabel_is_person__unionX__out_lang__out_nameX__in_labelX() {
            ComputerTestHelper.compute("g.V.choose(__.label.is('person'), union(__.out.lang, __.out.name), __.in.label)", g)
        }

        public Traversal<Vertex, Map<String, Long>> get_g_V_chooseXlabel_is_person__unionX__out_lang__out_nameX__in_labelX_groupCount() {
            ComputerTestHelper.compute("g.V.choose(__.label.is('person'), union(__.out.lang, __.out.name), __.in.label).groupCount", g)
        }

        public Traversal<Vertex, Map<String, Long>> get_g_V_unionXrepeatXunionXoutXcreatedX__inXcreatedXX_timesX2X__repeatXunionXinXcreatedX__outXcreatedXX_timesX2XX_label_groupCount() {
            ComputerTestHelper.compute("""
            g.V.union(
                    repeat(union(
                            out('created'),
                            __.in('created'))).times(2),
                    repeat(union(
                            __.in('created'),
                            out('created'))).times(2)).label.groupCount()
           """, g)
        }

        @Override
        public Traversal<Vertex, Number> get_g_VX1_2X_unionXoutE_count__inE_count__outE_weight_sumX(
                final Object v1Id, final Object v2Id) {
            g.V(v1Id, v2Id).union(outE().count, inE().count, outE().weight.sum);
        }
    }
}
