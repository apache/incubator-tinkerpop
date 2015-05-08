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
package org.apache.tinkerpop.gremlin.process.traversal.step.filter

import org.apache.tinkerpop.gremlin.process.traversal.Traversal
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.structure.Vertex

import java.util.List
import java.util.Map

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.limit
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold
import static org.apache.tinkerpop.gremlin.process.traversal.Scope.global
import static org.apache.tinkerpop.gremlin.process.traversal.Scope.local

/**
 * @author Matt Frantz (http://github.com/mhfrantz)
 */
public abstract class GroovyTailTest {

    public static class Traversals extends TailTest {

        @Override
        public Traversal<Vertex, Object> get_g_V_id_order_tailXglobal_2X() {
            g.V.id.order.tail(global, 2)
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_id_order_tailX2X() {
            g.V.id.order.tail(2)
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_id_order_tailX7X() {
            g.V.id.order.tail(7)
        }

        @Override
        public Traversal<Vertex, List<Object>> get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_id_foldX_tailXlocal_2X() {
            g.V.as('a').out.as('a').out.as('a').select('a').by(unfold().id.fold).tail(local, 2)
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXunfold_id_foldX_tailXlocal_1X() {
            g.V.as('a').out.as('a').out.as('a').select('a').by(unfold().id.fold).tail(local, 1)
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_asXaX_out_asXaX_out_asXaX_selectXaX_byXlimitXlocal_0XX_tailXlocal_1X() {
            g.V.as('a').out.as('a').out.as('a').select('a').by(limit(local, 0)).tail(local, 1)
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_asXaX_out_asXbX_out_asXcX_select_byXT_idX_tailXlocal_2X() {
            g.V.as('a').out.as('b').out.as('c').select.by(T.id).tail(local, 2)
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_asXaX_out_asXbX_out_asXcX_select_byXT_idX_tailXlocal_1X() {
            g.V.as('a').out.as('b').out.as('c').select.by(T.id).tail(local, 1)
        }
    }
}
