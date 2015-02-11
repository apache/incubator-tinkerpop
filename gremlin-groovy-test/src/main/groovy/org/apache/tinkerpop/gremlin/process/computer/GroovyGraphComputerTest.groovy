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
package com.tinkerpop.gremlin.process.computer

import com.tinkerpop.gremlin.process.computer.lambda.LambdaMapReduce
import com.tinkerpop.gremlin.process.computer.lambda.LambdaVertexProgram
import com.tinkerpop.gremlin.process.computer.traversal.TraversalVertexProgram

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GroovyGraphComputerTest {

    public static class ComputerTest extends GraphComputerTest {
        public GraphComputer get_g_compute() {
            g.compute();
        }

        @Override
        public GraphComputer get_g_compute_setupXX_executeXX_terminateXtrueX_memoryKeysXset_incr_and_orX() {
            g.compute().program(LambdaVertexProgram.build().memoryComputeKeys("set", "incr", "and", "or").create());
        }

        @Override
        public GraphComputer get_g_compute_setupXX_executeXX_terminateXtrueX_memoryKeysXnullX() {
            g.compute().program(LambdaVertexProgram.build().memoryComputeKeys([null] as Set).create());
        }

        @Override
        public GraphComputer get_g_compute_setupXX_executeXX_terminateXtrueX_memoryKeysX_X() {
            g.compute().program(LambdaVertexProgram.build().memoryComputeKeys('').create());
        }

        @Override
        public GraphComputer get_g_compute_setupXsetXa_trueXX_executeXX_terminateXtrueX() {
            g.compute().program(LambdaVertexProgram.build().setup("gremlin-groovy", "a.set('a', true)").create());
        }

        @Override
        public GraphComputer get_g_compute_setupXX_executeXX_terminateXtrueX() {
            g.compute().program(LambdaVertexProgram.build().create());
        }

        @Override
        public GraphComputer get_g_compute_setupXX_executeXv_blah_m_incrX_terminateX1X_elementKeysXnameLengthCounterX_memoryKeysXa_bX() {
            return g.compute().program(LambdaVertexProgram.build().
                    execute("gremlin-groovy", """
                        import static org.junit.Assert.*;
                        try {
                            a.property("blah", "blah");
                            fail("Should throw an IllegalArgumentException");
                        } catch (IllegalArgumentException e) {
                            assertEquals(GraphComputer.Exceptions.providedKeyIsNotAnElementComputeKey("blah").getMessage(), e.getMessage());
                        } catch (Exception e) {
                            fail("Should throw an IllegalArgumentException: " + e);
                        }

                        c.incr("a", 1);
                        if (c.isInitialIteration()) {
                            a.property("nameLengthCounter", a.<String>value("name").length());
                            c.incr("b", a.<String>value("name").length());
                        } else {
                            a.property(VertexProperty.Cardinality.single,"nameLengthCounter", a.<String>value("name").length() + a.<Integer>value("nameLengthCounter"));
                        }
                    """).terminate("gremlin-groovy", "a.getIteration() == 1")
                    .elementComputeKeys("nameLengthCounter").
                    memoryComputeKeys("a", "b").create());
        }

        @Override
        public GraphComputer get_g_compute_setupXabcdeX_executeXtestMemoryX_terminateXtestMemoryXmemoryKeysXabcdeX() {
            return g.compute().program(LambdaVertexProgram.build().
                    setup("gremlin-groovy", """
                        a.set("a", 0l);
                        a.set("b", 0l);
                        a.set("c", true);
                        a.set("d", false);
                        a.set("e", true);
                    """).
                    execute("gremlin-groovy", """
                        import static org.junit.Assert.*;
                        // test current step values
                        assertEquals(Long.valueOf(6 * c.getIteration()), c.get("a"));
                        assertEquals(Long.valueOf(0), c.get("b"));
                        if (c.isInitialIteration()) {
                            assertTrue(c.get("c"));
                            assertFalse(c.get("d"));
                        } else {
                            assertFalse(c.get("c"));
                            assertTrue(c.get("d"));
                        }
                        assertTrue(c.get("e"));

                        // update current step values and make sure returns are correct
                        assertEquals(Long.valueOf(6 * c.getIteration()) + 1l, c.incr("a", 1l));
                        assertEquals(Long.valueOf(0) + 1l, c.incr("b", 1l));
                        assertFalse(c.and("c", false));
                        assertTrue(c.or("d", true));
                        assertFalse(c.and("e", false));

                        // test current step values, should be the same as previous prior to update
                        assertEquals(Long.valueOf(6 * c.getIteration()), c.get("a"));
                        assertEquals(Long.valueOf(0), c.get("b"));
                        if (c.isInitialIteration()) {
                            assertTrue(c.get("c"));
                            assertFalse(c.get("d"));
                        } else {
                            assertFalse(c.get("c"));
                            assertTrue(c.get("d"));
                        }
                        assertTrue(c.get("e"));
                    """).
                    terminate("gremlin-groovy", """
                        import static org.junit.Assert.*;
                        assertEquals(Long.valueOf(6 * (a.getIteration() + 1)), a.get("a"));
                        assertEquals(Long.valueOf(6), a.get("b"));
                        assertFalse(a.get("c"));
                        assertTrue(a.get("d"));
                        assertFalse(a.get("e"));
                        a.set("b", 0l);
                        a.set("e", true);
                        return a.getIteration() > 1;
                    """).
                    memoryComputeKeys("a", "b", "c", "d", "e").create());
        }

        @Override
        public GraphComputer get_g_compute_mapXageX_reduceXsumX_memoryXnextX_memoryKeyXageSumX() {
            g.compute().mapReduce(LambdaMapReduce.<MapReduce.NullObject, Integer, MapReduce.NullObject, Integer, Integer> build()
                    .map("gremlin-groovy", "if(a.property('age').isPresent()) b.emit(a.value('age'))")
                    .reduce("gremlin-groovy", "c.emit(b.sum())")
                    .memory("gremlin-groovy", "a.next().getValue()")
                    .memoryKey("ageSum").create());
        }

        @Override
        public GraphComputer get_g_compute_executeXcounterX_terminateX8X_mapreduceXcounter_aX_mapreduceXcounter_bX() {
            return g.compute().program(LambdaVertexProgram.build()
                    .execute("gremlin-groovy", "a.property(VertexProperty.Cardinality.single,'counter', c.isInitialIteration() ? 1 : a.value('counter') + 1)")
                    .terminate("gremlin-groovy", "a.getIteration() > 8")
                    .elementComputeKeys(["counter"] as Set).create())
                    .mapReduce(LambdaMapReduce.<MapReduce.NullObject, Integer, MapReduce.NullObject, Integer, Integer> build()
                    .map("gremlin-groovy", "b.emit(a.value('counter'))")
                    .reduce("gremlin-groovy",
                    """
                        int counter = 0;
                        while (b.hasNext()) {
                            counter = counter + b.next();
                        }
                        c.emit(MapReduce.NullObject.instance(), counter);
                        """)
                    .memory("gremlin-groovy", "a.next().getValue()")
                    .memoryKey("a").create())
                    .mapReduce(LambdaMapReduce.<MapReduce.NullObject, Integer, MapReduce.NullObject, Integer, Integer> build()
                    .map("gremlin-groovy", "b.emit(a.value('counter'))")
                    .combine("gremlin-groovy", "c.emit(1)")
                    .reduce("gremlin-groovy", "c.emit(1)")
                    .memory("gremlin-groovy", "a.next().getValue()")
                    .memoryKey("b").create());

        }

        @Override
        public GraphComputer get_g_compute_mapXidX_reduceXidX_reduceKeySortXreverseX_memoryKeyXidsX() {
            return g.compute().mapReduce(LambdaMapReduce.<Long, Long, Long, Long, List<Long>> build()
                    .map("b.emit(a.id() as Long, a.id() as Long)")
                    .reduce("b.forEachRemaining{c.emit(it, it)}")
                    .memoryKey("ids")
                    .reduceKeySort("Comparator.reverseOrder()")
                    .memory("""
                        temp = []
                        a.forEachRemaining{temp.add(it.getKey())}
                        temp
                    """)
                    .create());
        }

        @Override
        public GraphComputer get_g_compute_programXTraversalVertexProgram_build_traversalXg_V_both_hasXlabel_personX_age_groupCountXaXX_create() {
            return g.compute().program(TraversalVertexProgram.build().
                    traversal("GraphFactory.open(['gremlin.graph':'${g.metaClass.theClass.getCanonicalName()}']).V().both().has(label,'person').values('age').groupCount('a')").
                    create());
        }
    }
}
