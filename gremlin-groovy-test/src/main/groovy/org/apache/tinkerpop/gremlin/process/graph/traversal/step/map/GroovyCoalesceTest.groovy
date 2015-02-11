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
package com.tinkerpop.gremlin.process.graph.traversal.step.map

import com.tinkerpop.gremlin.process.ComputerTestHelper
import com.tinkerpop.gremlin.process.Path
import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.process.Traversal
import com.tinkerpop.gremlin.structure.Vertex

import static com.tinkerpop.gremlin.process.graph.traversal.__.out
import static com.tinkerpop.gremlin.process.graph.traversal.__.outE

/**
 *
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public abstract class GroovyCoalesceTest {

    public static class StandardTest extends CoalesceTest {

        @Override
        public Traversal<Vertex, Vertex> get_g_V_coalesceXoutXfooX_outXbarXX() {
            return g.V().coalesce(out('foo'), out('bar'));
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_coalesceXoutXknowsX_outXcreatedXX_valuesXnameX(final Object v1Id) {
            return g.V(v1Id).coalesce(out('knows'), out('created')).values('name');
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_coalesceXoutXcreatedX_outXknowsXX_valuesXnameX(final Object v1Id) {
            return g.V(v1Id).coalesce(out('created'), out('knows')).values('name');
        }

        @Override
        public Traversal<Vertex, Map<String, Long>> get_g_V_coalesceXoutXlikesX_outXknowsX_inXcreatedXX_groupCount_byXnameX() {
            return g.V.coalesce(out('likes'), out('knows'), out('created')).groupCount().by('name').cap();
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_coalesceXoutEXknowsX_outEXcreatedXX_otherV_path_byXnameX_byXlabelX() {
            return g.V.coalesce(outE('knows'), outE('created')).otherV.path.by('name').by(T.label);
        }
    }

    public static class ComputerTest extends CoalesceTest {

        @Override
        public Traversal<Vertex, Vertex> get_g_V_coalesceXoutXfooX_outXbarXX() {
            ComputerTestHelper.compute("g.V().coalesce(out('foo'), out('bar'))", g)
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_coalesceXoutXknowsX_outXcreatedXX_valuesXnameX(final Object v1Id) {
            ComputerTestHelper.compute("g.V(${v1Id}).coalesce(out('knows'), out('created')).values('name')", g)
        }

        @Override
        public Traversal<Vertex, String> get_g_VX1X_coalesceXoutXcreatedX_outXknowsXX_valuesXnameX(final Object v1Id) {
            ComputerTestHelper.compute("g.V(${v1Id}).coalesce(out('created'), out('knows')).values('name')", g)
        }

        @Override
        public Traversal<Vertex, Map<String, Long>> get_g_V_coalesceXoutXlikesX_outXknowsX_inXcreatedXX_groupCount_byXnameX() {
            ComputerTestHelper.compute("g.V().coalesce(out('likes'), out('knows'), out('created')).groupCount().by('name').cap()", g)
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_coalesceXoutEXknowsX_outEXcreatedXX_otherV_path_byXnameX_byXlabelX() {
            ComputerTestHelper.compute("g.V().coalesce(outE('knows'), outE('created')).otherV().path().by('name').by(T.label)", g)
        }
    }
}
