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

import org.apache.tinkerpop.gremlin.process.traversal.Path
import org.apache.tinkerpop.gremlin.process.traversal.Traversal
import org.apache.tinkerpop.gremlin.process.traversal.util.ScriptTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public abstract class GroovyDedupTest {

    public static class Traversals extends DedupTest {

        @Override
        public Traversal<Vertex, String> get_g_V_both_dedup_name() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.both.dedup.name");
        }

        @Override
        public Traversal<Vertex, Map<String, List<Double>>> get_g_V_group_byXlabelX_byXbothE_weight_dedup_foldX() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.group.by(label).by(__.bothE.weight.dedup.fold)");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_both_hasXlabel_softwareX_dedup_byXlangX_name() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.both.has(label, 'software').dedup.by('lang').name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_both_name_order_byXa_bX_dedup_value() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V().both().properties('name').order.by { a, b -> a.value() <=> b.value() }.dedup.value");
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_both_both_dedup() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.both.both.dedup")
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_both_both_dedup_byXlabelX() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.both.both.dedup.by(label)")
        }

        @Override
        public Traversal<Vertex, String> get_g_V_both_both_name_dedup() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.both.both.name.dedup")
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_asXaX_both_asXbX_dedupXa_bX_byXlabelX_selectXa_bX() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.as('a').both.as('b').dedup('a', 'b').by(label).select('a','b')")
        }

        @Override
        public Traversal<Vertex, Path> get_g_V_asXaX_outXcreatedX_asXbX_inXcreatedX_asXcX_dedupXa_bX_path() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.as('a').out('created').as('b').in('created').as('c').dedup('a','b').path")
        }

        @Override
        Traversal<Vertex, String> get_g_V_outE_asXeX_inV_asXvX_selectXeX_order_byXweight_incrX_selectXvX_valuesXnameX_dedup() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.outE.as('e').inV.as('v').select('e').order.by('weight', incr).select('v').values('name').dedup")
        }

        @Override
        public Traversal<Vertex, String> get_g_V_both_both_dedup_byXoutE_countX_name() {
            new ScriptTraversal<>(g, "gremlin-groovy", "g.V.both.both.dedup.by(outE().count).name")
        }
    }
}
