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
package org.apache.tinkerpop.gremlin.process.traversal.step.map

import org.apache.tinkerpop.gremlin.process.UseEngine
import org.apache.tinkerpop.gremlin.process.computer.ComputerTestHelper
import org.apache.tinkerpop.gremlin.process.traversal.Path
import org.apache.tinkerpop.gremlin.process.traversal.Traversal
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.junit.Test

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GroovyPathTest {

    @UseEngine(TraversalEngine.Type.STANDARD)
    public static class StandardTraversals extends PathTest {

        @Override
        public Traversal<Vertex, Path> get_g_VX1X_name_path(final Object v1Id) {
            g.V(v1Id).identity.name.path
        }

        @Override
        public Traversal<Vertex, Path> get_g_VX1X_out_path_byXageX_byXnameX(final Object v1Id) {
            g.V(v1Id).out.path.by('age').by('name');
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_repeatXoutX_timesX2X_path_by_byXnameX_byXlangX() {
            g.V.repeat(__.out).times(2).path.by.by('name').by('lang');
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_out_out_path_byXnameX_byXageX() {
            g.V.out.out.path.by('name').by('age');
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_asXaX_hasXname_markoX_asXbX_hasXage_29X_asXcX_path() {
            g.V.as('a').has('name', 'marko').as('b').has('age', 29).as('c').path;
        }
    }

    @UseEngine(TraversalEngine.Type.COMPUTER)
    public static class ComputerTraversals extends PathTest {

        @Override
        public Traversal<Vertex, Path> get_g_VX1X_name_path(final Object v1Id) {
            ComputerTestHelper.compute("g.V(v1Id).identity.name.path", g, "v1Id", v1Id);
        }

        @Override
        @Test
        @org.junit.Ignore("Traversal not supported by ComputerTraversalEngine.computer")
        public void g_VX1X_out_path_byXageX_byXnameX() {
        }

        @Override
        @Test
        @org.junit.Ignore("Traversal not supported by ComputerTraversalEngine.computer")
        void g_V_repeatXoutX_timesX2X_path_byXitX_byXnameX_byXlangX() {
        }

        @Override
        @Test
        @org.junit.Ignore("Traversal not supported by ComputerTraversalEngine.computer")
        public void g_V_out_out_path_byXnameX_byXageX() {
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_asXaX_hasXname_markoX_asXbX_hasXage_29X_asXcX_path() {
            ComputerTestHelper.compute("g.V.as('a').has('name', 'marko').as('b').has('age', 29).as('c').path", g);
        }

        @Override
        Traversal<Vertex, Path> get_g_VX1X_out_path_byXageX_byXnameX(Object v1Id) {
            // override with nothing until the test itself is supported
            return null
        }

        @Override
        Traversal<Vertex, Path> get_g_V_repeatXoutX_timesX2X_path_by_byXnameX_byXlangX() {
            // override with nothing until the test itself is supported
            return null
        }

        @Override
        Traversal<Vertex, Path> get_g_V_out_out_path_byXnameX_byXageX() {
            // override with nothing until the test itself is supported
            return null
        }
    }
}
