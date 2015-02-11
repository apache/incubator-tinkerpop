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
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.branch;

import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.util.MapHelper;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.apache.tinkerpop.gremlin.process.graph.traversal.__.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class ChooseTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, Object> get_g_V_chooseXout_countX_optionX2L__nameX_optionX3L__valueMapX();

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_chooseXout_countX_optionX2L__nameX_optionX3L__valueMapX() {
        final Traversal<Vertex, Object> traversal = get_g_V_chooseXout_countX_optionX2L__nameX_optionX3L__valueMapX();
        printTraversalForm(traversal);
        Map<String, Long> counts = new HashMap<>();
        int counter = 0;
        while (traversal.hasNext()) {
            MapHelper.incr(counts, traversal.next().toString(), 1l);
            counter++;
        }
        assertFalse(traversal.hasNext());
        assertEquals(2, counter);
        assertEquals(2, counts.size());
        assertEquals(Long.valueOf(1), counts.get("{name=[marko], age=[29]}"));
        assertEquals(Long.valueOf(1), counts.get("josh"));
    }

    public static class StandardTest extends ChooseTest {

        @Override
        public Traversal<Vertex, Object> get_g_V_chooseXout_countX_optionX2L__nameX_optionX3L__valueMapX() {
            return g.V().choose(out().count())
                    .option(2L, values("name"))
                    .option(3L, valueMap());
        }
    }

    public static class ComputerTest extends ChooseTest {

        public ComputerTest() {
            requiresGraphComputer = true;
        }

        @Override
        public Traversal<Vertex, Object> get_g_V_chooseXout_countX_optionX2L__nameX_optionX3L__valueMapX() {
            return g.V().choose(out().count())
                    .option(2L, values("name"))
                    .option(3L, valueMap()).submit(g.compute());
        }
    }
}