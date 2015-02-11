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
package com.tinkerpop.gremlin.structure;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.process.Path;
import com.tinkerpop.gremlin.process.util.metric.TraversalMetrics;
import com.tinkerpop.gremlin.structure.io.graphson.GraphSONTokens;
import com.tinkerpop.gremlin.structure.io.kryo.KryoMapper;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Serialization tests that occur at a lower level than IO.  Note that there is no need to test GraphML here as
 * it is not a format that can be used for generalized serialization (i.e. it only serializes an entire graph).
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@RunWith(Enclosed.class)
public class SerializationTest {
    public static class KryoTest extends AbstractGremlinTest {
        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeVertexAsDetached() throws Exception {
            final KryoMapper kryoMapper = g.io().kryoMapper().create();
            final Kryo kryo = kryoMapper.createMapper();
            final Vertex v = g.V(convertToVertexId("marko")).next();
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final Output output = new Output(stream);
            kryo.writeObject(output, v);
            output.close();

            final Input input = new Input(stream.toByteArray());
            final Vertex detached = kryo.readObject(input, Vertex.class);
            input.close();
            assertNotNull(detached);
            assertEquals(v.label(), detached.label());
            assertEquals(v.id(), detached.id());
            assertEquals(v.value("name").toString(), detached.value("name"));
            assertEquals((Integer) v.value("age"), detached.value("age"));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeEdgeAsDetached() throws Exception {
            final KryoMapper kryoMapper = g.io().kryoMapper().create();
            final Kryo kryo = kryoMapper.createMapper();
            final Edge e = g.E(convertToEdgeId("marko", "knows", "vadas")).next();
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final Output output = new Output(stream);
            kryo.writeObject(output, e);
            output.close();

            final Input input = new Input(stream.toByteArray());
            final Edge detached = kryo.readObject(input, Edge.class);
            assertNotNull(detached);
            assertEquals(e.label(), detached.label());
            assertEquals(e.id(), detached.id());
            assertEquals((Double) e.value("weight"), detached.value("weight"));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializePropertyAsDetached() throws Exception {
            final KryoMapper kryoMapper = g.io().kryoMapper().create();
            final Kryo kryo = kryoMapper.createMapper();
            final Property p = g.E(convertToEdgeId("marko", "knows", "vadas")).next().property("weight");
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final Output output = new Output(stream);
            kryo.writeObject(output, p);
            output.close();

            final Input input = new Input(stream.toByteArray());
            final Property detached = kryo.readObject(input, Property.class);
            assertNotNull(detached);
            assertEquals(p.key(), detached.key());
            assertEquals(p.value(), detached.value());
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeVertexPropertyAsDetached() throws Exception {
            final KryoMapper kryoMapper = g.io().kryoMapper().create();
            final Kryo kryo = kryoMapper.createMapper();
            final VertexProperty vp = g.V(convertToVertexId("marko")).next().property("name");
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final Output output = new Output(stream);
            kryo.writeObject(output, vp);
            output.close();

            final Input input = new Input(stream.toByteArray());
            final VertexProperty detached = kryo.readObject(input, VertexProperty.class);
            input.close();
            assertNotNull(detached);
            assertEquals(vp.label(), detached.label());
            assertEquals(vp.id(), detached.id());
            assertEquals(vp.value(), detached.value());
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.CREW)
        public void shouldSerializeVertexPropertyWithPropertiesAsDetached() throws Exception {
            final KryoMapper kryoMapper = g.io().kryoMapper().create();
            final Kryo kryo = kryoMapper.createMapper();
            final VertexProperty vp = g.V(convertToVertexId("marko")).next().iterators().propertyIterator("location").next();
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final Output output = new Output(stream);
            kryo.writeObject(output, vp);
            output.close();

            final Input input = new Input(stream.toByteArray());
            final VertexProperty detached = kryo.readObject(input, VertexProperty.class);
            input.close();
            assertNotNull(detached);
            assertEquals(vp.label(), detached.label());
            assertEquals(vp.id(), detached.id());
            assertEquals(vp.value(), detached.value());
            assertEquals(vp.iterators().propertyIterator("startTime").next().value(), detached.iterators().propertyIterator("startTime").next().value());
            assertEquals(vp.iterators().propertyIterator("startTime").next().key(), detached.iterators().propertyIterator("startTime").next().key());
            assertEquals(vp.iterators().propertyIterator("endTime").next().value(), detached.iterators().propertyIterator("endTime").next().value());
            assertEquals(vp.iterators().propertyIterator("endTime").next().key(), detached.iterators().propertyIterator("endTime").next().key());
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializePathAsDetached() throws Exception {
            final KryoMapper kryoMapper = g.io().kryoMapper().create();
            final Kryo kryo = kryoMapper.createMapper();
            final Path p = g.V(convertToVertexId("marko")).as("a").outE().as("b").inV().as("c").path()
                    .filter(t -> ((Vertex) t.get().objects().get(2)).value("name").equals("lop")).next();
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final Output output = new Output(stream);
            kryo.writeObject(output, p);
            output.close();

            final Input input = new Input(stream.toByteArray());
            final Path detached = kryo.readObject(input, Path.class);
            input.close();
            assertNotNull(detached);
            assertEquals(p.labels().size(), detached.labels().size());
            assertEquals(p.labels().get(0).size(), detached.labels().get(0).size());
            assertEquals(p.labels().get(1).size(), detached.labels().get(1).size());
            assertEquals(p.labels().get(2).size(), detached.labels().get(2).size());
            assertTrue(p.labels().stream().flatMap(Collection::stream).allMatch(detached::hasLabel));

            final Vertex vOut = p.get("a");
            final Vertex detachedVOut = detached.get("a");
            assertEquals(vOut.label(), detachedVOut.label());
            assertEquals(vOut.id(), detachedVOut.id());

            // this is a SimpleTraverser so no properties are present in detachment
            assertFalse(detachedVOut.iterators().propertyIterator().hasNext());

            final Edge e = p.get("b");
            final Edge detachedE = detached.get("b");
            assertEquals(e.label(), detachedE.label());
            assertEquals(e.id(), detachedE.id());

            // this is a SimpleTraverser so no properties are present in detachment
            assertFalse(detachedE.iterators().propertyIterator().hasNext());

            final Vertex vIn = p.get("c");
            final Vertex detachedVIn = detached.get("c");
            assertEquals(vIn.label(), detachedVIn.label());
            assertEquals(vIn.id(), detachedVIn.id());

            // this is a SimpleTraverser so no properties are present in detachment
            assertFalse(detachedVIn.iterators().propertyIterator().hasNext());
        }
    }

    public static class GraphSONTest extends AbstractGremlinTest {
        private final TypeReference<HashMap<String, Object>> mapTypeReference = new TypeReference<HashMap<String, Object>>() {
        };

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeVertex() throws Exception {
            final ObjectMapper mapper = g.io().graphSONMapper().create().createMapper();
            final Vertex v = g.V(convertToVertexId("marko")).next();
            final String json = mapper.writeValueAsString(v);
            final Map<String, Object> m = mapper.readValue(json, mapTypeReference);

            assertEquals(GraphSONTokens.VERTEX, m.get(GraphSONTokens.TYPE));
            assertEquals(v.label(), m.get(GraphSONTokens.LABEL));
            assertNotNull(m.get(GraphSONTokens.ID));
            assertEquals(v.value("name").toString(), ((Map) ((List) ((Map) m.get(GraphSONTokens.PROPERTIES)).get("name")).get(0)).get(GraphSONTokens.VALUE));
            assertEquals((Integer) v.value("age"), ((Map) ((List) ((Map) m.get(GraphSONTokens.PROPERTIES)).get("age")).get(0)).get(GraphSONTokens.VALUE));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeEdge() throws Exception {
            final ObjectMapper mapper = g.io().graphSONMapper().create().createMapper();
            final Edge e = g.E(convertToEdgeId("marko", "knows", "vadas")).next();
            final String json = mapper.writeValueAsString(e);
            final Map<String, Object> m = mapper.readValue(json, mapTypeReference);

            assertEquals(GraphSONTokens.EDGE, m.get(GraphSONTokens.TYPE));
            assertEquals(e.label(), m.get(GraphSONTokens.LABEL));
            assertNotNull(m.get(GraphSONTokens.ID));
            assertEquals((Double) e.value("weight"), ((Map) m.get(GraphSONTokens.PROPERTIES)).get("weight"));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeProperty() throws Exception {
            final ObjectMapper mapper = g.io().graphSONMapper().create().createMapper();
            final Property p = g.E(convertToEdgeId("marko", "knows", "vadas")).next().property("weight");
            final String json = mapper.writeValueAsString(p);
            final Map<String, Object> m = mapper.readValue(json, mapTypeReference);

            assertEquals(p.value(), m.get(GraphSONTokens.VALUE));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeVertexProperty() throws Exception {
            final ObjectMapper mapper = g.io().graphSONMapper().create().createMapper();
            final VertexProperty vp = g.V(convertToVertexId("marko")).next().property("name");
            final String json = mapper.writeValueAsString(vp);
            final Map<String, Object> m = mapper.readValue(json, mapTypeReference);

            assertEquals(vp.label(), m.get(GraphSONTokens.LABEL));
            assertNotNull(m.get(GraphSONTokens.ID));
            assertEquals(vp.value(), m.get(GraphSONTokens.VALUE));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.CREW)
        public void shouldSerializeVertexPropertyWithProperties() throws Exception {
            final ObjectMapper mapper = g.io().graphSONMapper().create().createMapper();
            final VertexProperty vp = g.V(convertToVertexId("marko")).next().iterators().propertyIterator("location").next();
            final String json = mapper.writeValueAsString(vp);
            final Map<String, Object> m = mapper.readValue(json, mapTypeReference);

            assertEquals(vp.label(), m.get(GraphSONTokens.LABEL));
            assertNotNull(m.get(GraphSONTokens.ID));
            assertEquals(vp.value(), m.get(GraphSONTokens.VALUE));
            assertEquals(vp.iterators().propertyIterator("startTime").next().value(), ((Map) m.get(GraphSONTokens.PROPERTIES)).get("startTime"));
            assertEquals(vp.iterators().propertyIterator("endTime").next().value(), ((Map) m.get(GraphSONTokens.PROPERTIES)).get("endTime"));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializePath() throws Exception {
            final ObjectMapper mapper = g.io().graphSONMapper().create().createMapper();
            final Path p = g.V(convertToVertexId("marko")).as("a").outE().as("b").inV().as("c").path()
                    .filter(t -> ((Vertex) t.get().objects().get(2)).value("name").equals("lop")).next();
            final String json = mapper.writeValueAsString(p);
            final Map<String, Object> m = mapper.readValue(json, mapTypeReference);

            assertEquals(2, m.size());

            final List<List<String>> labels = (List<List<String>>) m.get(GraphSONTokens.LABELS);
            assertEquals(3, labels.size());
            assertEquals("a", labels.get(0).get(0));
            assertEquals(1, labels.get(0).size());
            assertEquals("b", labels.get(1).get(0));
            assertEquals(1, labels.get(1).size());
            assertEquals("c", labels.get(2).get(0));
            assertEquals(1, labels.get(2).size());

            final List<Object> objects = (List<Object>) m.get(GraphSONTokens.OBJECTS);
            assertEquals(3, objects.size());
            assertEquals("marko", ((Map) ((List) ((Map) ((Map) objects.get(0)).get(GraphSONTokens.PROPERTIES)).get("name")).get(0)).get(GraphSONTokens.VALUE));
            assertEquals("created", ((Map) objects.get(1)).get(GraphSONTokens.LABEL));
            assertEquals("lop", ((Map) ((List) ((Map) ((Map) objects.get(2)).get(GraphSONTokens.PROPERTIES)).get("name")).get(0)).get(GraphSONTokens.VALUE));
        }

        @Test
        @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
        public void shouldSerializeTraversalMetrics() throws Exception {
            final ObjectMapper mapper = g.io().graphSONMapper().create().createMapper();
            final TraversalMetrics tm = (TraversalMetrics) g.V().both().profile().cap(TraversalMetrics.METRICS_KEY).next();
            final String json = mapper.writeValueAsString(tm);
            final Map<String, Object> m = mapper.readValue(json, mapTypeReference);

            assertTrue(m.containsKey(GraphSONTokens.DURATION));
            assertTrue(m.containsKey(GraphSONTokens.METRICS));

            final List<Map<String, Object>> metrics = (List<Map<String, Object>>) m.get(GraphSONTokens.METRICS);
            assertEquals(3, metrics.size());

            final Map<String, Object> metrics0 = metrics.get(0);
            assertTrue(metrics0.containsKey(GraphSONTokens.ID));
            assertTrue(metrics0.containsKey(GraphSONTokens.NAME));
            assertTrue(metrics0.containsKey(GraphSONTokens.COUNT));
            assertTrue(metrics0.containsKey(GraphSONTokens.DURATION));
            assertTrue(metrics0.containsKey(GraphSONTokens.PERCENT_DURATION));
            assertTrue(metrics0.containsKey(GraphSONTokens.METRICS));

            final Map<String, Object> nestedMetrics = (Map<String, Object>) ((List<?>)metrics0.get(GraphSONTokens.METRICS)).get(0);
            assertTrue(nestedMetrics.containsKey(GraphSONTokens.ID));
            assertTrue(nestedMetrics.containsKey(GraphSONTokens.NAME));
            assertTrue(nestedMetrics.containsKey(GraphSONTokens.COUNT));
            assertTrue(nestedMetrics.containsKey(GraphSONTokens.DURATION));
            assertTrue(nestedMetrics.containsKey(GraphSONTokens.PERCENT_DURATION));
        }
    }
}
