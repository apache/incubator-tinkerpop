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
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.map

import org.apache.tinkerpop.gremlin.process.Scope
import org.apache.tinkerpop.gremlin.process.Traversal
import org.apache.tinkerpop.gremlin.process.ComputerTestHelper
import org.apache.tinkerpop.gremlin.process.graph.traversal.step.map.OrderTest
import org.apache.tinkerpop.gremlin.structure.Order
import org.apache.tinkerpop.gremlin.structure.Vertex

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GroovyOrderTest {


    public static class StandardTest extends OrderTest {

        @Override
        public Traversal<Vertex, String> get_g_V_name_order() {
            g.V().name.order()
        }

        @Override
        public Traversal<Vertex, String> get_g_V_name_order_byXabX() {
            g.V.name.order.by { a, b -> b <=> a }
        }

        @Override
        public Traversal<Vertex, String> get_g_V_name_order_byXa1_b1X_byXb2_a2X() {
            g.V.name.order.by { a, b -> a[1] <=> b[1] }.by { a, b -> b[2] <=> a[2] }
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXname_incrX_name() {
            g.V.order.by('name', Order.incr).name
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXnameX_name() {
            g.V.order.by('name', Order.incr).name
        }

        @Override
        public Traversal<Vertex, Double> get_g_V_outE_order_byXweight_decrX_weight() {
            g.V.outE.order.by('weight', Order.decr).weight
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXname_a1_b1X_byXname_b2_a2X_name() {
            return g.V.order.by('name', { a, b -> a[1].compareTo(b[1]) }).by('name', { a, b -> b[2].compareTo(a[2]) }).name;
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_select() {
            g.V.as('a').out('created').as('b').order.by(Order.shuffle).select();
        }

        @Override
        public Traversal<Vertex, Map<Integer, Integer>> get_g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalueDecrX_byXkeyIncrX(
                final Object v1Id) {
            g.V(v1Id).map {
                final Map map = [:];
                map[1] = it.age;
                map[2] = it.age * 2;
                map[3] = it.age * 3;
                map[4] = it.age;
                return map;
            }.order(Scope.local).by(Order.valueDecr).by(Order.keyIncr);
        }
    }

    public static class ComputerTest extends OrderTest {

        @Override
        public Traversal<Vertex, String> get_g_V_name_order() {
            ComputerTestHelper.compute("g.V().name.order()", g)
        }

        @Override
        public Traversal<Vertex, String> get_g_V_name_order_byXabX() {
            ComputerTestHelper.compute("g.V.name.order.by { a, b -> b <=> a }", g)
        }

        @Override
        public Traversal<Vertex, String> get_g_V_name_order_byXa1_b1X_byXb2_a2X() {
            ComputerTestHelper.compute("g.V.name.order.by { a, b -> a[1] <=> b[1] }.by{ a, b -> b[2] <=> a[2] }", g)
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXname_incrX_name() {
            g.V.order.by('name', Order.incr).name
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXnameX_name() {
            g.V.order.by('name', Order.incr).name
        }

        @Override
        public Traversal<Vertex, Double> get_g_V_outE_order_byXweight_decrX_weight() {
            g.V.outE.order.by('weight', Order.decr).weight
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXname_a1_b1X_byXname_b2_a2X_name() {
            return g.V.order.by('name') { a, b -> a[1].compareTo(b[1]) }.by('name') { a, b -> b[2].compareTo(a[2]) }.name;
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_select() {
            ComputerTestHelper.compute("g.V.as('a').out('created').as('b').order.by(Order.shuffle).select()", g);
        }

        @Override
        public Traversal<Vertex, Map<Integer, Integer>> get_g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalueDecrX_byXkeyIncrX(
                final Object v1Id) {
            ComputerTestHelper.compute("""
            g.V(${v1Id}).map {
                final Map map = [:];
                map[1] = it.age;
                map[2] = it.age * 2;
                map[3] = it.age * 3;
                map[4] = it.age;
                return map;
            }.order(Scope.local).by(Order.valueDecr).by(Order.keyIncr);
            """, g)
        }

    }
}
