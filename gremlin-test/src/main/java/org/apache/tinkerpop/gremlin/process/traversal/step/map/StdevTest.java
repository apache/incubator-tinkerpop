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
package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.GremlinProcessRunner;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Junshi Guo
 */
@RunWith(GremlinProcessRunner.class)
public abstract class StdevTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Double> get_g_V_age_stdev();

    public abstract Traversal<Vertex, Double> get_g_V_age_fold_stdevXlocalX();

    public abstract Traversal<Vertex, Number> get_g_V_foo_stdev();

    public abstract Traversal<Vertex, Number> get_g_V_foo_fold_stdevXlocalX();

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_age_stdev() {
        for (final Traversal<Vertex, Double> traversal : Arrays.asList(get_g_V_age_stdev(), get_g_V_age_fold_stdevXlocalX())) {
            printTraversalForm(traversal);
            final Double stdev = traversal.next();
            assertEquals(3.03, stdev, 0.05);
            assertFalse(traversal.hasNext());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_foo_stdev() {
        for (final Traversal<Vertex, Number> traversal : Arrays.asList(get_g_V_foo_stdev(), get_g_V_foo_fold_stdevXlocalX())) {
            printTraversalForm(traversal);
            assertFalse(traversal.hasNext());
        }
    }

    public static class Traversals extends StdevTest {

        @Override
        public Traversal<Vertex, Double> get_g_V_age_stdev() {
            return g.V().values("age").stdev();
        }

        @Override
        public Traversal<Vertex, Double> get_g_V_age_fold_stdevXlocalX() {
            return g.V().values("age").fold().stdev(Scope.local);
        }

        @Override
        public Traversal<Vertex, Number> get_g_V_foo_stdev() {
            return g.V().values("foo").stdev();
        }

        @Override
        public Traversal<Vertex, Number> get_g_V_foo_fold_stdevXlocalX() {
            return g.V().values("foo").fold().stdev(Scope.local);
        }
    }

}
