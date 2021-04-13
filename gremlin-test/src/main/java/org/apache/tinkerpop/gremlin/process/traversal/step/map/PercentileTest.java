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
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Junshi Guo
 */
public abstract class PercentileTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Object> get_g_V_age_percentileX50X();

    public abstract Traversal<Vertex, Object> get_g_V_age_fold_percentileXlocal_50X();

    public abstract Traversal<Vertex, Object> get_g_V_foo_percentileX50X();

    public abstract Traversal<Vertex, Object> get_g_V_foo_fold_percentileXlocal_50X();

    public abstract Traversal<Vertex, Object> get_g_V_age_percentileX25_75X();

    public abstract Traversal<Vertex, Object> get_g_V_age_fold_percentileXlocal_25_75X();

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_age_percentileX50X() {
        for (final Traversal<Vertex, Object> traversal : Arrays.asList(get_g_V_age_percentileX50X(), get_g_V_age_fold_percentileXlocal_50X())) {
            printTraversalForm(traversal);
            final Object percentile50 = traversal.next();
            assertTrue(percentile50 instanceof Number);
            assertEquals(29, percentile50);
            assertFalse(traversal.hasNext());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_foo_percentileX50X() {
        for (final Traversal<Vertex, Object> traversal : Arrays.asList(get_g_V_foo_percentileX50X(), get_g_V_foo_fold_percentileXlocal_50X())) {
            printTraversalForm(traversal);
            assertFalse(traversal.hasNext());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_age_percentileX25_75X() {
        for (final Traversal<Vertex, Object> traversal : Arrays.asList(get_g_V_age_percentileX25_75X(), get_g_V_age_fold_percentileXlocal_25_75X())) {
            printTraversalForm(traversal);
            final Object percentiles = traversal.next();
            assertTrue(percentiles instanceof Map);
            assertEquals(27, ((Map) percentiles).get(25));
            assertEquals(32, ((Map) percentiles).get(75));
            assertFalse(traversal.hasNext());
        }
    }

    public static class Traversals extends PercentileTest {
        @Override
        public Traversal<Vertex, Object> get_g_V_age_percentileX50X() {
            return g.V().values("age").percentile(50);
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_age_fold_percentileXlocal_50X() {
            return g.V().values("age").fold().percentile(Scope.local, 50);
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_foo_percentileX50X() {
            return g.V().values("foo").percentile(50);
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_foo_fold_percentileXlocal_50X() {
            return g.V().values("foo").fold().percentile(Scope.local, 50);
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_age_percentileX25_75X() {
            return g.V().values("age").percentile(25, 75);
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_age_fold_percentileXlocal_25_75X() {
            return g.V().values("age").fold().percentile(Scope.local, 25, 75);
        }
    }
}
