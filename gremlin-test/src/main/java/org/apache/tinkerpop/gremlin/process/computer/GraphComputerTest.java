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
package org.apache.tinkerpop.gremlin.process.computer;

import org.apache.tinkerpop.gremlin.ExceptionCoverage;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.computer.util.StaticMapReduce;
import org.apache.tinkerpop.gremlin.process.computer.util.StaticVertexProgram;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@ExceptionCoverage(exceptionClass = GraphComputer.Exceptions.class, methods = {
        "providedKeyIsNotAMemoryComputeKey",
        "computerHasNoVertexProgramNorMapReducers",
        "computerHasAlreadyBeenSubmittedAVertexProgram",
        "providedKeyIsNotAnElementComputeKey",
        "incidentAndAdjacentElementsCanNotBeAccessedInMapReduce",
        "adjacentVertexLabelsCanNotBeRead",
        "adjacentVertexPropertiesCanNotBeReadOrUpdated",
        "adjacentVertexEdgesAndVerticesCanNotBeReadOrUpdated",
        "resultGraphPersistCombinationNotSupported",
        "vertexPropertiesCanNotBeUpdatedInMapReduce"
})
@ExceptionCoverage(exceptionClass = Graph.Exceptions.class, methods = {
        "graphDoesNotSupportProvidedGraphComputer"
})
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class GraphComputerTest extends AbstractGremlinProcessTest {

    @Test
    @LoadGraphWith(MODERN)
    public void shouldHaveStandardStringRepresentation() {
        final GraphComputer computer = graph.compute(graphComputerClass.get());
        assertEquals(StringFactory.graphComputerString(computer), computer.toString());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void shouldNotAllowWithNoVertexProgramNorMapReducers() throws Exception {
        try {
            graph.compute(graphComputerClass.get()).submit().get();
            fail("Should throw an IllegalStateException when there is no vertex program nor map reducers");
        } catch (Exception ex) {
            validateException(GraphComputer.Exceptions.computerHasNoVertexProgramNorMapReducers(), ex);
        }
    }

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldNotAllowBadGraphComputers() {
        try {
            graph.compute(BadGraphComputer.class);
            fail("Providing a bad graph computer class should fail");
        } catch (Exception ex) {
            validateException(Graph.Exceptions.graphDoesNotSupportProvidedGraphComputer(BadGraphComputer.class), ex);
        }
    }

    public static class BadGraphComputer implements GraphComputer {

        @Override
        public GraphComputer result(final ResultGraph resultGraph) {
            return null;
        }

        @Override
        public GraphComputer persist(final Persist persist) {
            return null;
        }

        @Override
        public GraphComputer program(final VertexProgram vertexProgram) {
            return null;
        }

        @Override
        public GraphComputer mapReduce(final MapReduce mapReduce) {
            return null;
        }

        @Override
        public Future<ComputerResult> submit() {
            return null;
        }

    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldHaveImmutableComputeResultMemory() throws Exception {
        final ComputerResult results = graph.compute(graphComputerClass.get()).program(new VertexProgramB()).submit().get();

        try {
            results.memory().set("set", "test");
        } catch (Exception ex) {
            validateException(Memory.Exceptions.memoryIsCurrentlyImmutable(), ex);
        }

        try {
            results.memory().incr("incr", 1);
        } catch (Exception ex) {
            validateException(Memory.Exceptions.memoryIsCurrentlyImmutable(), ex);
        }

        try {
            results.memory().and("and", true);
        } catch (Exception ex) {
            validateException(Memory.Exceptions.memoryIsCurrentlyImmutable(), ex);
        }

        try {
            results.memory().or("or", false);
        } catch (Exception ex) {
            validateException(Memory.Exceptions.memoryIsCurrentlyImmutable(), ex);
        }
    }

    public static class VertexProgramB extends StaticVertexProgram {
        @Override
        public void setup(final Memory memory) {
            assertEquals(0, memory.getIteration());
            assertTrue(memory.isInitialIteration());
        }

        @Override
        public void execute(final Vertex vertex, final Messenger messenger, final Memory memory) {
            assertEquals(0, memory.getIteration());
            assertTrue(memory.isInitialIteration());
        }

        @Override
        public boolean terminate(final Memory memory) {
            assertEquals(0, memory.getIteration());
            assertTrue(memory.isInitialIteration());
            return true;
        }

        @Override
        public Set<String> getMemoryComputeKeys() {
            return new HashSet<>(Arrays.asList("set", "incr", "and", "or"));
        }

        @Override
        public Set<MessageScope> getMessageScopes(final Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.ORIGINAL;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    /////////////////////////////////////////////

    @Test
    @LoadGraphWith(MODERN)
    public void shouldNotAllowNullMemoryKeys() throws Exception {
        try {
            graph.compute(graphComputerClass.get()).program(new VertexProgramC()).submit().get();
            fail("Providing null memory key should fail");
        } catch (Exception ex) {
            // validateException(Memory.Exceptions.memoryKeyCanNotBeNull(), ex);
        }
    }

    public static class VertexProgramC extends StaticVertexProgram {
        @Override
        public void setup(final Memory memory) {

        }

        @Override
        public void execute(final Vertex vertex, final Messenger messenger, final Memory memory) {

        }

        @Override
        public boolean terminate(final Memory memory) {
            return true;
        }

        @Override
        public Set<String> getMemoryComputeKeys() {
            return new HashSet<>(Arrays.asList(null));
        }

        @Override
        public Set<MessageScope> getMessageScopes(final Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.ORIGINAL;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldNotAllowEmptyMemoryKeys() throws Exception {
        try {
            graph.compute(graphComputerClass.get()).program(new VertexProgramD()).submit().get();
            fail("Providing empty memory key should fail");
        } catch (Exception ex) {
            validateException(Memory.Exceptions.memoryKeyCanNotBeEmpty(), ex);
        }
    }

    public static class VertexProgramD extends StaticVertexProgram {
        @Override
        public void setup(final Memory memory) {

        }

        @Override
        public void execute(final Vertex vertex, final Messenger messenger, final Memory memory) {

        }

        @Override
        public boolean terminate(final Memory memory) {
            return true;
        }

        @Override
        public Set<String> getMemoryComputeKeys() {
            return new HashSet<>(Arrays.asList(""));
        }

        @Override
        public Set<MessageScope> getMessageScopes(final Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.ORIGINAL;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    ////////////////////////////////////////////

    ////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldNotAllowSettingUndeclaredMemoryKeys() throws Exception {
        graph.compute(graphComputerClass.get()).program(new VertexProgramE()).submit().get();
    }

    public static class VertexProgramE extends StaticVertexProgram {
        @Override
        public void setup(final Memory memory) {
            try {
                memory.set("a", true);
                fail("Setting a memory key that wasn't declared should fail");
            } catch (IllegalArgumentException e) {
                assertEquals(GraphComputer.Exceptions.providedKeyIsNotAMemoryComputeKey("a").getMessage(), e.getMessage());
            }
        }

        @Override
        public void execute(final Vertex vertex, final Messenger messenger, final Memory memory) {

        }

        @Override
        public boolean terminate(final Memory memory) {
            return true;
        }


        @Override
        public Set<MessageScope> getMessageScopes(final Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.ORIGINAL;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    ////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldNotAllowTheSameComputerToExecutedTwice() throws Exception {
        final GraphComputer computer = graph.compute(graphComputerClass.get()).program(new VertexProgramA());
        computer.submit().get(); // this should work as its the first run of the graph computer

        try {
            computer.submit(); // this should fail as the computer has already been executed
            fail("Using the same graph computer to compute again should not be possible");
        } catch (IllegalStateException e) {

        } catch (Exception e) {
            fail("Should yield an illegal state exception for graph computer being executed twice");
        }

        // test no rerun of graph computer
        try {
            computer.submit(); // this should fail as the computer has already been executed even through new program submitted
            fail("Using the same graph computer to compute again should not be possible");
        } catch (IllegalStateException e) {

        } catch (Exception e) {
            fail("Should yield an illegal state exception for graph computer being executed twice");
        }
    }

    public static class VertexProgramA extends StaticVertexProgram {

        @Override
        public void setup(final Memory memory) {

        }

        @Override
        public void execute(final Vertex vertex, final Messenger messenger, final Memory memory) {

        }

        @Override
        public boolean terminate(final Memory memory) {
            return true;
        }

        @Override
        public Set<MessageScope> getMessageScopes(final Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.ORIGINAL;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldHaveConsistentMemoryVertexPropertiesAndExceptions() throws Exception {
        ComputerResult results = graph.compute(graphComputerClass.get()).program(new VertexProgramF()).submit().get();
        assertEquals(1, results.memory().getIteration());
        assertEquals(2, results.memory().asMap().size());
        assertEquals(2, results.memory().keys().size());
        assertTrue(results.memory().keys().contains("a"));
        assertTrue(results.memory().keys().contains("b"));
        assertTrue(results.memory().getRuntime() >= 0);

        assertEquals(Long.valueOf(12), results.memory().<Long>get("a"));   // 2 iterations
        assertEquals(Long.valueOf(28), results.memory().<Long>get("b"));
        try {
            results.memory().get("BAD");
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(Memory.Exceptions.memoryDoesNotExist("BAD").getMessage(), e.getMessage());
        }
        assertEquals(Long.valueOf(0), results.graph().traversal().V().count().next()); // persist new/nothing.

        results.graph().traversal().V().forEachRemaining(v -> {
            assertTrue(v.property("nameLengthCounter").isPresent());
            assertEquals(Integer.valueOf(v.<String>value("name").length() * 2), Integer.valueOf(v.<Integer>value("nameLengthCounter")));
        });
    }

    public static class VertexProgramF extends StaticVertexProgram<Object> {

        @Override
        public void setup(final Memory memory) {

        }

        @Override
        public void execute(final Vertex vertex, final Messenger<Object> messenger, final Memory memory) {
            try {
                vertex.property(VertexProperty.Cardinality.single, "blah", "blah");
                fail("Should throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
                assertEquals(GraphComputer.Exceptions.providedKeyIsNotAnElementComputeKey("blah").getMessage(), e.getMessage());
            } catch (final Exception e) {
                fail("Should throw an IllegalArgumentException: " + e);
            }

            memory.incr("a", 1);
            if (memory.isInitialIteration()) {
                vertex.property(VertexProperty.Cardinality.single, "nameLengthCounter", vertex.<String>value("name").length());
                memory.incr("b", vertex.<String>value("name").length());
            } else {
                vertex.property(VertexProperty.Cardinality.single, "nameLengthCounter", vertex.<String>value("name").length() + vertex.<Integer>value("nameLengthCounter"));
            }
        }

        @Override
        public boolean terminate(final Memory memory) {
            return memory.getIteration() == 1;
        }

        @Override
        public Set<String> getElementComputeKeys() {
            return new HashSet<>(Arrays.asList("nameLengthCounter"));
        }

        @Override
        public Set<String> getMemoryComputeKeys() {
            return new HashSet<>(Arrays.asList("a", "b"));
        }

        @Override
        public Set<MessageScope> getMessageScopes(Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.NEW;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldAndOrIncrCorrectlyThroughSubStages() throws Exception {
        ComputerResult results = graph.compute(graphComputerClass.get()).program(new VertexProgramG()).submit().get();
        assertEquals(2, results.memory().getIteration());
        assertEquals(6, results.memory().asMap().size());
        assertEquals(6, results.memory().keys().size());
        assertTrue(results.memory().keys().contains("a"));
        assertTrue(results.memory().keys().contains("b"));
        assertTrue(results.memory().keys().contains("c"));
        assertTrue(results.memory().keys().contains("d"));
        assertTrue(results.memory().keys().contains("e"));
        assertTrue(results.memory().keys().contains("f"));

        assertEquals(Long.valueOf(18), results.memory().get("a"));
        assertEquals(Long.valueOf(0), results.memory().get("b"));
        assertFalse(results.memory().get("c"));
        assertTrue(results.memory().get("d"));
        assertTrue(results.memory().get("e"));
        assertEquals(3, results.memory().<Integer>get("f").intValue());
    }

    public static class VertexProgramG extends StaticVertexProgram {

        @Override
        public void setup(final Memory memory) {
            memory.set("a", 0l);
            memory.set("b", 0l);
            memory.set("c", true);
            memory.set("d", false);
            memory.set("e", true);
            memory.set("f", memory.getIteration());
        }

        @Override
        public void execute(Vertex vertex, Messenger messenger, Memory memory) {
            // test current step values
            assertEquals(Long.valueOf(6 * memory.getIteration()), memory.get("a"));
            assertEquals(Long.valueOf(0), memory.get("b"));
            if (memory.isInitialIteration()) {
                assertTrue(memory.get("c"));
                assertFalse(memory.get("d"));
            } else {
                assertFalse(memory.get("c"));
                assertTrue(memory.get("d"));
            }
            assertTrue(memory.get("e"));
            assertEquals(memory.getIteration(), memory.<Integer>get("f").intValue());

            // update current step values
            memory.incr("a", 1l);
            memory.incr("b", 1l);
            memory.and("c", false);
            memory.or("d", true);
            memory.and("e", false);
            memory.set("f", memory.getIteration() + 1);

            // test current step values, should be the same as previous prior to update
            assertEquals(Long.valueOf(6 * memory.getIteration()), memory.get("a"));
            assertEquals(Long.valueOf(0), memory.get("b"));
            if (memory.isInitialIteration()) {
                assertTrue(memory.get("c"));
                assertFalse(memory.get("d"));
            } else {
                assertFalse(memory.get("c"));
                assertTrue(memory.get("d"));
            }
            assertTrue(memory.get("e"));
            assertEquals(memory.getIteration(), memory.<Integer>get("f").intValue());
        }

        @Override
        public boolean terminate(Memory memory) {
            assertEquals(Long.valueOf(6 * (memory.getIteration() + 1)), memory.get("a"));
            assertEquals(Long.valueOf(6), memory.get("b"));
            assertFalse(memory.get("c"));
            assertTrue(memory.get("d"));
            assertFalse(memory.get("e"));
            assertEquals(memory.getIteration() + 1, memory.<Integer>get("f").intValue());
            memory.set("b", 0l);
            memory.set("e", true);
            return memory.getIteration() > 1;
        }

        @Override
        public Set<String> getElementComputeKeys() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getMemoryComputeKeys() {
            return new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f"));
        }

        @Override
        public Set<MessageScope> getMessageScopes(Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.NEW;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldAllowMapReduceWithNoVertexProgram() throws Exception {
        final ComputerResult results = graph.compute(graphComputerClass.get()).mapReduce(new MapReduceA()).submit().get();
        assertEquals(123, results.memory().<Integer>get("ageSum").intValue());
    }

    private static class MapReduceA extends StaticMapReduce<MapReduce.NullObject, Integer, MapReduce.NullObject, Integer, Integer> {

        @Override
        public boolean doStage(final Stage stage) {
            return stage.equals(Stage.MAP) || stage.equals(Stage.REDUCE);
        }

        @Override
        public void map(final Vertex vertex, final MapEmitter<NullObject, Integer> emitter) {
            vertex.<Integer>property("age").ifPresent(emitter::emit);
        }

        @Override
        public void reduce(NullObject key, Iterator<Integer> values, ReduceEmitter<NullObject, Integer> emitter) {
            int sum = 0;
            while (values.hasNext()) {
                sum = sum + values.next();
            }
            emitter.emit(sum);
        }

        @Override
        public Integer generateFinalResult(Iterator<KeyValue<NullObject, Integer>> keyValues) {
            return keyValues.next().getValue();
        }

        @Override
        public String getMemoryKey() {
            return "ageSum";
        }
    }
    /////////////////////////////////////////////

    @Test
    @LoadGraphWith(MODERN)
    public void shouldSupportMultipleMapReduceJobs() throws Exception {
        final ComputerResult results = graph.compute(graphComputerClass.get())
                .program(new VertexProgramH())
                .mapReduce(new MapReduceH1())
                .mapReduce(new MapReduceH2()).submit().get();
        assertEquals(60, results.memory().<Integer>get("a").intValue());
        assertEquals(1, results.memory().<Integer>get("b").intValue());
    }

    public static class VertexProgramH extends StaticVertexProgram {

        @Override
        public void setup(final Memory memory) {

        }

        @Override
        public void execute(Vertex vertex, Messenger messenger, Memory memory) {
            vertex.property(VertexProperty.Cardinality.single, "counter", memory.isInitialIteration() ? 1 : vertex.<Integer>value("counter") + 1);
        }

        @Override
        public boolean terminate(final Memory memory) {
            return memory.getIteration() > 8;
        }

        @Override
        public Set<String> getElementComputeKeys() {
            return new HashSet<>(Arrays.asList("counter"));
        }

        @Override
        public Set<String> getMemoryComputeKeys() {
            return Collections.emptySet();
        }

        @Override
        public Set<MessageScope> getMessageScopes(Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.NEW;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }

    private static class MapReduceH1 extends StaticMapReduce<MapReduce.NullObject, Integer, MapReduce.NullObject, Integer, Integer> {

        @Override
        public boolean doStage(final Stage stage) {
            return stage.equals(Stage.MAP) || stage.equals(Stage.REDUCE);
        }

        @Override
        public void map(final Vertex vertex, final MapEmitter<NullObject, Integer> emitter) {
            vertex.<Integer>property("counter").ifPresent(emitter::emit);
        }

        @Override
        public void reduce(final NullObject key, final Iterator<Integer> values, final ReduceEmitter<NullObject, Integer> emitter) {
            int sum = 0;
            while (values.hasNext()) {
                sum = sum + values.next();
            }
            emitter.emit(sum);
        }

        @Override
        public Integer generateFinalResult(final Iterator<KeyValue<NullObject, Integer>> keyValues) {
            return keyValues.next().getValue();
        }

        @Override
        public String getMemoryKey() {
            return "a";
        }
    }

    private static class MapReduceH2 extends StaticMapReduce<Integer, Integer, Integer, Integer, Integer> {

        @Override
        public boolean doStage(final Stage stage) {
            return true;
        }

        @Override
        public void map(final Vertex vertex, final MapEmitter<Integer, Integer> emitter) {
            vertex.<Integer>property("age").ifPresent(age -> emitter.emit(age, age));
        }

        @Override
        public void combine(Integer key, Iterator<Integer> values, ReduceEmitter<Integer, Integer> emitter) {
            values.forEachRemaining(i -> emitter.emit(i, 1));
        }

        @Override
        public void reduce(Integer key, Iterator<Integer> values, ReduceEmitter<Integer, Integer> emitter) {
            values.forEachRemaining(i -> emitter.emit(i, 1));
        }

        @Override
        public Integer generateFinalResult(Iterator<KeyValue<Integer, Integer>> keyValues) {
            return keyValues.next().getValue();
        }

        @Override
        public String getMemoryKey() {
            return "b";
        }
    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldSortReduceOutput() throws Exception {
        final ComputerResult results = graph.compute(graphComputerClass.get()).mapReduce(new MapReduceB()).submit().get();
        final List<Long> ids = results.memory().get("ids");
        assertEquals(6, ids.size());
        for (int i = 1; i < ids.size(); i++) {
            assertTrue(ids.get(i) < ids.get(i - 1));
        }
    }

    public static class MapReduceB extends StaticMapReduce<Long, Long, Long, Long, List<Long>> {

        @Override
        public boolean doStage(final Stage stage) {
            return stage.equals(Stage.REDUCE) || stage.equals(Stage.MAP);
        }

        @Override
        public void map(final Vertex vertex, final MapEmitter<Long, Long> emitter) {
            emitter.emit(Long.valueOf(vertex.id().toString()), Long.valueOf(vertex.id().toString()));
        }

        @Override
        public void reduce(Long key, Iterator<Long> values, ReduceEmitter<Long, Long> emitter) {
            values.forEachRemaining(id -> emitter.emit(id, id));
        }

        @Override
        public Optional<Comparator<Long>> getReduceKeySort() {
            return Optional.of(Comparator.<Long>reverseOrder());
        }

        @Override
        public String getMemoryKey() {
            return "ids";
        }

        @Override
        public List<Long> generateFinalResult(final Iterator<KeyValue<Long, Long>> keyValues) {
            final List<Long> list = new ArrayList<>();
            keyValues.forEachRemaining(id -> list.add(id.getKey()));
            return list;
        }
    }
    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldSortMapOutput() throws Exception {
        final ComputerResult results = graph.compute(graphComputerClass.get()).mapReduce(new MapReduceBB()).submit().get();
        final List<Long> ids = results.memory().get("ids");
        assertEquals(6, ids.size());
        for (int i = 1; i < ids.size(); i++) {
            assertTrue(ids.get(i) < ids.get(i - 1));
        }
    }

    public static class MapReduceBB extends StaticMapReduce<Long, Long, Long, Long, List<Long>> {

        @Override
        public boolean doStage(final Stage stage) {
            return stage.equals(Stage.MAP);
        }

        @Override
        public void map(final Vertex vertex, final MapEmitter<Long, Long> emitter) {
            emitter.emit(Long.valueOf(vertex.id().toString()), Long.valueOf(vertex.id().toString()));
        }

        @Override
        public Optional<Comparator<Long>> getMapKeySort() {
            return Optional.of(Comparator.<Long>reverseOrder());
        }

        @Override
        public String getMemoryKey() {
            return "ids";
        }

        @Override
        public List<Long> generateFinalResult(final Iterator<KeyValue<Long, Long>> keyValues) {
            final List<Long> list = new ArrayList<>();
            keyValues.forEachRemaining(id -> list.add(id.getKey()));
            return list;
        }
    }


    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldOnlyAllowReadingVertexPropertiesInMapReduce() throws Exception {
        graph.compute(graphComputerClass.get()).mapReduce(new MapReduceC()).submit().get();
    }

    public static class MapReduceC extends StaticMapReduce<MapReduce.NullObject, MapReduce.NullObject, MapReduce.NullObject, MapReduce.NullObject, MapReduce.NullObject> {

        @Override
        public boolean doStage(final Stage stage) {
            return stage.equals(Stage.MAP);
        }

        @Override
        public void map(final Vertex vertex, final MapEmitter<MapReduce.NullObject, MapReduce.NullObject> emitter) {
            try {
                vertex.edges(Direction.OUT);
                fail("Edges should not be accessible in MapReduce.map()");
            } catch (final UnsupportedOperationException e) {
                assertEquals(GraphComputer.Exceptions.incidentAndAdjacentElementsCanNotBeAccessedInMapReduce().getMessage(), e.getMessage());
            }
            try {
                vertex.edges(Direction.IN);
                fail("Edges should not be accessible in MapReduce.map()");
            } catch (final UnsupportedOperationException e) {
                assertEquals(GraphComputer.Exceptions.incidentAndAdjacentElementsCanNotBeAccessedInMapReduce().getMessage(), e.getMessage());
            }
            try {
                vertex.edges(Direction.BOTH);
                fail("Edges should not be accessible in MapReduce.map()");
            } catch (final UnsupportedOperationException e) {
                assertEquals(GraphComputer.Exceptions.incidentAndAdjacentElementsCanNotBeAccessedInMapReduce().getMessage(), e.getMessage());
            }
            ////
            try {
                vertex.property("name", "bob");
                fail("Vertex properties should be immutable in MapReduce.map()");
            } catch (final UnsupportedOperationException e) {
                assertEquals(GraphComputer.Exceptions.vertexPropertiesCanNotBeUpdatedInMapReduce().getMessage(), e.getMessage());
            }
            try {
                vertex.property("name").property("test", 1);
                fail("Vertex properties should be immutable in MapReduce.map()");
            } catch (final UnsupportedOperationException e) {
                assertEquals(GraphComputer.Exceptions.vertexPropertiesCanNotBeUpdatedInMapReduce().getMessage(), e.getMessage());
            }

        }

        @Override
        public String getMemoryKey() {
            return MapReduce.NullObject.instance().toString();
        }

        @Override
        public MapReduce.NullObject generateFinalResult(final Iterator<KeyValue<MapReduce.NullObject, MapReduce.NullObject>> keyValues) {
            return MapReduce.NullObject.instance();
        }
    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldOnlyAllowIDAccessOfAdjacentVertices() throws Exception {
        graph.compute(graphComputerClass.get()).program(new VertexProgramI()).submit().get();
    }

    public static class VertexProgramI extends StaticVertexProgram<MapReduce.NullObject> {

        @Override
        public void setup(final Memory memory) {

        }

        @Override
        public void execute(Vertex vertex, Messenger messenger, Memory memory) {
            vertex.vertices(Direction.OUT).forEachRemaining(Vertex::id);
            vertex.vertices(Direction.IN).forEachRemaining(Vertex::id);
            vertex.vertices(Direction.BOTH).forEachRemaining(Vertex::id);
            if (vertex.vertices(Direction.OUT).hasNext()) {
                try {
                    vertex.vertices(Direction.OUT).forEachRemaining(Vertex::label);
                    fail("Adjacent vertex labels should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexLabelsCanNotBeRead().getMessage(), e.getMessage());
                }
            }
            if (vertex.vertices(Direction.IN).hasNext()) {
                try {
                    vertex.vertices(Direction.IN).forEachRemaining(Vertex::label);
                    fail("Adjacent vertex labels should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexLabelsCanNotBeRead().getMessage(), e.getMessage());
                }
            }
            if (vertex.vertices(Direction.BOTH).hasNext()) {
                try {
                    vertex.vertices(Direction.BOTH).forEachRemaining(Vertex::label);
                    fail("Adjacent vertex labels should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexLabelsCanNotBeRead().getMessage(), e.getMessage());
                }
            }
            ////////////////////
            if (vertex.vertices(Direction.OUT).hasNext()) {
                try {
                    vertex.vertices(Direction.OUT).forEachRemaining(v -> v.property("name"));
                    fail("Adjacent vertex properties should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexPropertiesCanNotBeReadOrUpdated().getMessage(), e.getMessage());
                }
            }
            if (vertex.vertices(Direction.IN).hasNext()) {
                try {
                    vertex.vertices(Direction.IN).forEachRemaining(v -> v.property("name"));
                    fail("Adjacent vertex properties should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexPropertiesCanNotBeReadOrUpdated().getMessage(), e.getMessage());
                }
            }
            if (vertex.vertices(Direction.BOTH).hasNext()) {
                try {
                    vertex.vertices(Direction.BOTH).forEachRemaining(v -> v.property("name"));
                    fail("Adjacent vertex properties should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexPropertiesCanNotBeReadOrUpdated().getMessage(), e.getMessage());
                }
            }
            ////////////////////
            if (vertex.vertices(Direction.BOTH).hasNext()) {
                try {
                    vertex.vertices(Direction.BOTH).forEachRemaining(v -> v.edges(Direction.BOTH));
                    fail("Adjacent vertex edges should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexEdgesAndVerticesCanNotBeReadOrUpdated().getMessage(), e.getMessage());
                }
            }
            if (vertex.vertices(Direction.BOTH).hasNext()) {
                try {
                    vertex.vertices(Direction.BOTH).forEachRemaining(v -> v.vertices(Direction.BOTH));
                    fail("Adjacent vertex vertices should not be accessible in VertexProgram.execute()");
                } catch (UnsupportedOperationException e) {
                    assertEquals(GraphComputer.Exceptions.adjacentVertexEdgesAndVerticesCanNotBeReadOrUpdated().getMessage(), e.getMessage());
                }
            }

        }

        @Override
        public boolean terminate(final Memory memory) {
            return memory.getIteration() > 1;
        }

        @Override
        public Set<MessageScope> getMessageScopes(Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.NEW;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }
    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith(MODERN)
    public void shouldStartAndEndWorkersForVertexProgramAndMapReduce() throws Exception {
        VertexProgramJ.TIME_KEEPER.set(-1l);
        MapReduceI.TIME_KEEPER.set(-1l);
        MapReduceI.WORKER_START.clear();
        MapReduceI.WORKER_END.clear();
        assertEquals(3, graph.compute(graphComputerClass.get()).program(new VertexProgramJ()).mapReduce(new MapReduceI()).submit().get().memory().<Integer>get("a").intValue());
        assertEquals(Long.MIN_VALUE, VertexProgramJ.TIME_KEEPER.get());
        if (MapReduceI.WORKER_START.size() == 2) {
            assertEquals(2, MapReduceI.WORKER_START.size());
            assertTrue(MapReduceI.WORKER_START.contains(MapReduce.Stage.MAP) && MapReduceI.WORKER_START.contains(MapReduce.Stage.REDUCE));
        } else {
            assertEquals(3, MapReduceI.WORKER_START.size());
            assertTrue(MapReduceI.WORKER_START.contains(MapReduce.Stage.MAP) && MapReduceI.WORKER_START.contains(MapReduce.Stage.COMBINE) && MapReduceI.WORKER_START.contains(MapReduce.Stage.REDUCE));
        }
        if (MapReduceI.WORKER_END.size() == 2) {
            assertEquals(2, MapReduceI.WORKER_END.size());
            assertTrue(MapReduceI.WORKER_END.contains(MapReduce.Stage.MAP) && MapReduceI.WORKER_END.contains(MapReduce.Stage.REDUCE));
        } else {
            assertEquals(3, MapReduceI.WORKER_END.size());
            assertTrue(MapReduceI.WORKER_END.contains(MapReduce.Stage.MAP) && MapReduceI.WORKER_END.contains(MapReduce.Stage.COMBINE) && MapReduceI.WORKER_END.contains(MapReduce.Stage.REDUCE));
        }
    }

    public static class VertexProgramJ extends StaticVertexProgram {

        private static final AtomicLong TIME_KEEPER = new AtomicLong(-1l);

        @Override
        public void setup(final Memory memory) {
            memory.set("test", memory.getIteration());
        }

        @Override
        public void workerIterationStart(final Memory memory) {
            assertEquals(memory.getIteration(), memory.<Integer>get("test").intValue());
            final long time = System.nanoTime();
            if (!memory.isInitialIteration())
                assertNotEquals(-1l, TIME_KEEPER.get());
            assertTrue(TIME_KEEPER.getAndSet(time) <= time);
            try {
                memory.set("test", memory.getIteration());
                fail("Should throw an immutable memory exception");
            } catch (IllegalStateException e) {
                assertEquals(Memory.Exceptions.memoryIsCurrentlyImmutable().getMessage(), e.getMessage());
            }
        }

        @Override
        public void execute(Vertex vertex, Messenger messenger, Memory memory) {
            assertEquals(memory.getIteration(), memory.<Integer>get("test").intValue());
            memory.set("test", memory.getIteration() + 1);
            sleep(10);
            assertNotEquals(-1l, TIME_KEEPER.get());
            assertTrue(TIME_KEEPER.get() <= System.nanoTime());
        }

        @Override
        public boolean terminate(final Memory memory) {
            return memory.getIteration() > 3;
        }

        @Override
        public void workerIterationEnd(final Memory memory) {
            assertEquals(memory.getIteration(), memory.<Integer>get("test").intValue());
            assertNotEquals(-1l, TIME_KEEPER.get());
            assertTrue(TIME_KEEPER.getAndSet(Long.MIN_VALUE) <= System.nanoTime());
            try {
                memory.set("test", memory.getIteration());
                fail("Should throw an immutable memory exception");
            } catch (IllegalStateException e) {
                assertEquals(Memory.Exceptions.memoryIsCurrentlyImmutable().getMessage(), e.getMessage());
            }
        }

        @Override
        public Set<String> getMemoryComputeKeys() {
            return new HashSet<>(Arrays.asList("test"));
        }

        @Override
        public Set<MessageScope> getMessageScopes(Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.NEW;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.NOTHING;
        }
    }

    private static class MapReduceI extends StaticMapReduce<MapReduce.NullObject, Integer, MapReduce.NullObject, Integer, Integer> {

        private static final AtomicLong TIME_KEEPER = new AtomicLong(-1l);
        private static final Set<Stage> WORKER_START = new ConcurrentSkipListSet<>();
        private static final Set<Stage> WORKER_END = new ConcurrentSkipListSet<>();

        @Override
        public boolean doStage(final Stage stage) {
            return true;
        }

        @Override
        public void workerStart(final Stage stage) {
            final long time = System.nanoTime();
            if (!stage.equals(Stage.MAP)) assertNotEquals(-1l, TIME_KEEPER.get());
            assertTrue(TIME_KEEPER.getAndSet(time) <= time);
            WORKER_START.add(stage);
            if (!stage.equals(Stage.MAP))
                assertFalse(WORKER_END.isEmpty());
        }

        @Override
        public void map(final Vertex vertex, final MapEmitter<NullObject, Integer> emitter) {
            emitter.emit(1);
            sleep(10);
            assertNotEquals(-1l, TIME_KEEPER.get());
            final long time = System.nanoTime();
            assertTrue(TIME_KEEPER.getAndSet(time) <= time);
            assertEquals(1, WORKER_START.size());
            assertTrue(WORKER_START.contains(Stage.MAP));
        }

        @Override
        public void combine(final NullObject key, final Iterator<Integer> values, final ReduceEmitter<NullObject, Integer> emitter) {
            emitter.emit(2);
            sleep(10);
            assertNotEquals(-1l, TIME_KEEPER.get());
            final long time = System.nanoTime();
            assertTrue(TIME_KEEPER.getAndSet(time) <= time);
            assertEquals(2, WORKER_START.size());
            assertTrue(WORKER_START.contains(Stage.MAP) && WORKER_START.contains(Stage.COMBINE));
            assertFalse(WORKER_END.isEmpty());
        }

        @Override
        public void reduce(final NullObject key, final Iterator<Integer> values, final ReduceEmitter<NullObject, Integer> emitter) {
            emitter.emit(3);
            sleep(10);
            assertNotEquals(-1l, TIME_KEEPER.get());
            final long time = System.nanoTime();
            assertTrue(TIME_KEEPER.getAndSet(time) <= time);
            if (WORKER_START.size() == 2) {
                assertEquals(2, WORKER_START.size());
                assertTrue(WORKER_START.contains(Stage.MAP) && WORKER_START.contains(Stage.REDUCE));
            } else {
                assertEquals(3, WORKER_START.size());
                assertTrue(WORKER_START.contains(Stage.MAP) && WORKER_START.contains(Stage.COMBINE) && WORKER_START.contains(Stage.REDUCE));
            }
            assertFalse(WORKER_END.isEmpty());
        }

        @Override
        public void workerEnd(final Stage stage) {
            assertNotEquals(-1l, TIME_KEEPER.get());
            final long time = System.nanoTime();
            assertTrue(TIME_KEEPER.get() <= time);
            assertFalse(WORKER_START.isEmpty());
            if (!stage.equals(Stage.MAP))
                assertFalse(WORKER_END.isEmpty());
            WORKER_END.add(stage);
        }

        @Override
        public Integer generateFinalResult(final Iterator<KeyValue<NullObject, Integer>> keyValues) {
            assertEquals(3, keyValues.next().getValue().intValue());
            return 3;
        }

        @Override
        public String getMemoryKey() {
            return "a";
        }
    }

    /////////////////////////////////////////////////

    private static void sleep(final long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /////////////////////////////////////////////

    /////////////////////////////////////////////
    @Test
    @LoadGraphWith
    public void shouldSupportPersistResultGraphPairsStatedInFeatures() throws Exception {
        for (final GraphComputer.ResultGraph resultGraph : GraphComputer.ResultGraph.values()) {
            for (final GraphComputer.Persist persist : GraphComputer.Persist.values()) {
                final GraphComputer computer = graph.compute(graphComputerClass.get());
                if (computer.features().supportsResultGraphPersistCombination(resultGraph, persist)) {
                    computer.program(new VertexProgramK()).result(resultGraph).persist(persist).submit().get();
                } else {
                    try {
                        computer.program(new VertexProgramK()).result(resultGraph).persist(persist).submit().get();
                        fail("The GraphComputer " + computer + " states that it does support the following resultGraph/persist pair: " + resultGraph + ":" + persist);
                    } catch (final IllegalArgumentException e) {
                        assertEquals(GraphComputer.Exceptions.resultGraphPersistCombinationNotSupported(resultGraph, persist).getMessage(), e.getMessage());
                    }
                }
            }
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void shouldProcessResultGraphNewWithPersistNothing() throws Exception {
        final GraphComputer computer = graph.compute(graphComputerClass.get());
        if (computer.features().supportsResultGraphPersistCombination(GraphComputer.ResultGraph.NEW, GraphComputer.Persist.NOTHING)) {
            final ComputerResult result = computer.program(new VertexProgramK()).result(GraphComputer.ResultGraph.NEW).persist(GraphComputer.Persist.NOTHING).submit().get();
            assertEquals(Long.valueOf(0l), result.graph().traversal().V().count().next());
            assertEquals(Long.valueOf(0l), result.graph().traversal().E().count().next());
            assertEquals(Long.valueOf(0l), result.graph().traversal().V().values().count().next());
            assertEquals(Long.valueOf(0l), result.graph().traversal().E().values().count().next());
            assertEquals(Double.valueOf(0.0d), result.graph().traversal().V().values("money").sum().next());
            ///
            assertEquals(Long.valueOf(6l), graph.traversal().V().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().count().next());
            assertEquals(Long.valueOf(12l), graph.traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().values().count().next());
            assertEquals(Double.valueOf(0.0d), graph.traversal().V().values("money").sum().next());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void shouldProcessResultGraphNewWithPersistVertexProperties() throws Exception {
        final GraphComputer computer = graph.compute(graphComputerClass.get());
        if (computer.features().supportsResultGraphPersistCombination(GraphComputer.ResultGraph.NEW, GraphComputer.Persist.VERTEX_PROPERTIES)) {
            final ComputerResult result = computer.program(new VertexProgramK()).result(GraphComputer.ResultGraph.NEW).persist(GraphComputer.Persist.VERTEX_PROPERTIES).submit().get();
            assertEquals(Long.valueOf(6l), result.graph().traversal().V().count().next());
            assertEquals(Long.valueOf(0l), result.graph().traversal().E().count().next());
            assertEquals(Long.valueOf(18l), result.graph().traversal().V().values().count().next());
            assertEquals(Long.valueOf(0l), result.graph().traversal().E().values().count().next());
            assertEquals(Double.valueOf(28.0d), result.graph().traversal().V().values("money").sum().next());
            ///
            assertEquals(Long.valueOf(6l), graph.traversal().V().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().count().next());
            assertEquals(Long.valueOf(12l), graph.traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().values().count().next());
            assertEquals(Double.valueOf(0.0d), graph.traversal().V().values("money").sum().next());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void shouldProcessResultGraphNewWithPersistEdges() throws Exception {
        final GraphComputer computer = graph.compute(graphComputerClass.get());
        if (computer.features().supportsResultGraphPersistCombination(GraphComputer.ResultGraph.NEW, GraphComputer.Persist.EDGES)) {
            final ComputerResult result = computer.program(new VertexProgramK()).result(GraphComputer.ResultGraph.NEW).persist(GraphComputer.Persist.EDGES).submit().get();
            assertEquals(Long.valueOf(6l), result.graph().traversal().V().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().count().next());
            assertEquals(Long.valueOf(18l), result.graph().traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().values().count().next());
            assertEquals(Double.valueOf(28.0d), result.graph().traversal().V().values("money").sum().next());
            ///
            assertEquals(Long.valueOf(6l), graph.traversal().V().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().count().next());
            assertEquals(Long.valueOf(12l), graph.traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().values().count().next());
            assertEquals(Double.valueOf(0.0d), graph.traversal().V().values("money").sum().next());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void shouldProcessResultGraphOriginalWithPersistNothing() throws Exception {
        final GraphComputer computer = graph.compute(graphComputerClass.get());
        if (computer.features().supportsResultGraphPersistCombination(GraphComputer.ResultGraph.ORIGINAL, GraphComputer.Persist.NOTHING)) {
            final ComputerResult result = computer.program(new VertexProgramK()).result(GraphComputer.ResultGraph.ORIGINAL).persist(GraphComputer.Persist.NOTHING).submit().get();
            assertEquals(Long.valueOf(6l), result.graph().traversal().V().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().count().next());
            assertEquals(Long.valueOf(12l), result.graph().traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().values().count().next());
            assertEquals(Double.valueOf(0.0d), result.graph().traversal().V().values("money").sum().next());
            ///
            assertEquals(Long.valueOf(6l), graph.traversal().V().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().count().next());
            assertEquals(Long.valueOf(12l), graph.traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().values().count().next());
            assertEquals(Double.valueOf(0.0d), graph.traversal().V().values("money").sum().next());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void shouldProcessResultGraphOriginalWithPersistVertexProperties() throws Exception {
        final GraphComputer computer = graph.compute(graphComputerClass.get());
        if (computer.features().supportsResultGraphPersistCombination(GraphComputer.ResultGraph.ORIGINAL, GraphComputer.Persist.VERTEX_PROPERTIES)) {
            final ComputerResult result = computer.program(new VertexProgramK()).result(GraphComputer.ResultGraph.ORIGINAL).persist(GraphComputer.Persist.VERTEX_PROPERTIES).submit().get();
            assertEquals(Long.valueOf(6l), result.graph().traversal().V().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().count().next());
            assertEquals(Long.valueOf(18l), result.graph().traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().values().count().next());
            assertEquals(Double.valueOf(28.0d), result.graph().traversal().V().values("money").sum().next());
            ///
            assertEquals(Long.valueOf(6l), graph.traversal().V().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().count().next());
            assertEquals(Long.valueOf(18l), graph.traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().values().count().next());
            assertEquals(Double.valueOf(28.0d), graph.traversal().V().values("money").sum().next());
        }
    }

    @Test
    @LoadGraphWith(MODERN)
    public void shouldProcessResultGraphOriginalWithPersistEdges() throws Exception {
        final GraphComputer computer = graph.compute(graphComputerClass.get());
        if (computer.features().supportsResultGraphPersistCombination(GraphComputer.ResultGraph.ORIGINAL, GraphComputer.Persist.EDGES)) {
            final ComputerResult result = computer.program(new VertexProgramK()).result(GraphComputer.ResultGraph.ORIGINAL).persist(GraphComputer.Persist.EDGES).submit().get();
            assertEquals(Long.valueOf(6l), result.graph().traversal().V().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().count().next());
            assertEquals(Long.valueOf(18l), result.graph().traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), result.graph().traversal().E().values().count().next());
            assertEquals(Double.valueOf(28.0d), result.graph().traversal().V().values("money").sum().next());
            ///
            assertEquals(Long.valueOf(6l), graph.traversal().V().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().count().next());
            assertEquals(Long.valueOf(18l), graph.traversal().V().values().count().next());
            assertEquals(Long.valueOf(6l), graph.traversal().E().values().count().next());
            assertEquals(Double.valueOf(28.0d), graph.traversal().V().values("money").sum().next());
        }
    }

    public static class VertexProgramK extends StaticVertexProgram {


        @Override
        public void setup(final Memory memory) {

        }

        @Override
        public void execute(final Vertex vertex, final Messenger messenger, final Memory memory) {
            vertex.property("money", vertex.<String>value("name").length());
        }

        @Override
        public boolean terminate(final Memory memory) {
            return true;
        }

        @Override
        public Set<String> getElementComputeKeys() {
            return Collections.singleton("money");
        }

        @Override
        public Set<MessageScope> getMessageScopes(Memory memory) {
            return Collections.emptySet();
        }

        @Override
        public GraphComputer.ResultGraph getPreferredResultGraph() {
            return GraphComputer.ResultGraph.NEW;
        }

        @Override
        public GraphComputer.Persist getPreferredPersist() {
            return GraphComputer.Persist.EDGES;
        }
    }

    /////////////////////////////////////////////

}
