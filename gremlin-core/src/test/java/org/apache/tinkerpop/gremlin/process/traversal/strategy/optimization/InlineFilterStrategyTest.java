/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.and;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.as;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.dedup;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.drop;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.filter;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.limit;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.map;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.match;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.tail;
import static org.junit.Assert.assertEquals;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@RunWith(Parameterized.class)
public class InlineFilterStrategyTest {

    @Parameterized.Parameter(value = 0)
    public Traversal original;

    @Parameterized.Parameter(value = 1)
    public Traversal optimized;

    @Test
    public void doTest() {
        final TraversalStrategies strategies = new DefaultTraversalStrategies();
        strategies.addStrategies(InlineFilterStrategy.instance());
        this.original.asAdmin().setStrategies(strategies);
        this.original.asAdmin().applyStrategies();
        assertEquals(this.optimized, this.original);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> generateTestParameters() {

        return Arrays.asList(new Traversal[][]{
                {filter(out("knows")), filter(out("knows"))},
                {filter(has("age", P.gt(10))).as("a"), has("age", P.gt(10)).as("a")},
                {filter(has("age", P.gt(10)).as("b")).as("a"), has("age", P.gt(10)).as("b", "a")},
                {filter(has("age", P.gt(10)).as("a")), has("age", P.gt(10)).as("a")},
                {filter(and(has("age", P.gt(10)).as("a"), has("name", "marko"))), has("age", P.gt(10)).as("a").has("name", "marko")},
                //
                {and(has("age", P.gt(10)), filter(has("age", 22))), has("age", P.gt(10)).has("age", 22)},
                {and(has("age", P.gt(10)).as("a"), and(filter(has("age", 22).as("b")).as("c"), has("name", "marko").as("d"))), has("age", P.gt(10)).as("a").has("age", 22).as("b", "c").has("name", "marko").as("d")},
                {and(has("age", P.gt(10)), and(out("knows"), has("name", "marko"))), has("age", P.gt(10)).and(out("knows"), has("name", "marko"))},
                //
                {V().match(as("a").has("age", 10), as("a").filter(has("name")).as("b")), V().as("a").has("age", 10).match(as("a").has("name").as("b"))},
                {match(as("a").has("age", 10), as("a").filter(has("name")).as("b")), match(as("a").has("age", 10), as("a").has("name").as("b"))},
                {map(match(as("a").has("age", 10), as("a").filter(has("name")).as("b"))), map(match(as("a").has("age", 10), as("a").has("name").as("b")))},
                {V().match(as("a").has("age", 10)), V().as("a").has("age", 10)},
                {V().match(as("a").has("age", 10).as("b"), as("a").filter(has("name")).as("b")), V().as("a").has("age", 10).as("b").match(as("a").has("name").as("b"))},
                //
                {filter(dedup()), filter(dedup())},
                {filter(filter(drop())), filter(drop())},
                {and(has("name"), limit(10).has("age")), and(has("name"), limit(10).has("age"))},
                {filter(tail(10).as("a")), filter(tail(10).as("a"))},
        });
    }
}
