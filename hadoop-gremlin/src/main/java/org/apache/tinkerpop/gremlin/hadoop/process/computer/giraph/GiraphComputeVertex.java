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
package com.tinkerpop.gremlin.hadoop.process.computer.giraph;

import com.tinkerpop.gremlin.hadoop.Constants;
import com.tinkerpop.gremlin.hadoop.structure.io.ObjectWritable;
import com.tinkerpop.gremlin.process.computer.VertexProgram;
import com.tinkerpop.gremlin.process.computer.util.ComputerDataStrategy;
import com.tinkerpop.gremlin.process.computer.util.MapMemory;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.VertexProperty;
import com.tinkerpop.gremlin.structure.io.kryo.KryoReader;
import com.tinkerpop.gremlin.structure.io.kryo.KryoWriter;
import com.tinkerpop.gremlin.structure.strategy.StrategyVertex;
import com.tinkerpop.gremlin.structure.util.detached.DetachedEdge;
import com.tinkerpop.gremlin.structure.util.detached.DetachedVertex;
import com.tinkerpop.gremlin.structure.util.wrapped.WrappedVertex;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerVertex;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class GiraphComputeVertex extends Vertex<LongWritable, Text, NullWritable, ObjectWritable> implements WrappedVertex<TinkerVertex> {

    //TODO: Dangerous that the underlying TinkerGraph Vertex can have edges written to it.
    //TODO: LongWritable as the key is not general enough -- ObjectWritable causes problems though :|

    private static final String VERTEX_ID = Graph.Hidden.hide("giraph.gremlin.vertexId");
    private TinkerVertex tinkerVertex;
    private StrategyVertex wrappedVertex;
    private static KryoWriter KRYO_WRITER = KryoWriter.build().create();
    private static KryoReader KRYO_READER = KryoReader.build().create();

    public GiraphComputeVertex() {
    }

    public GiraphComputeVertex(final com.tinkerpop.gremlin.structure.Vertex vertex) {
        this.tinkerVertex = GiraphComputeVertex.generateTinkerVertexForm(vertex);
        this.tinkerVertex.graph().variables().set(VERTEX_ID, this.tinkerVertex.id());
        this.initialize(new LongWritable(Long.valueOf(this.tinkerVertex.id().toString())), this.deflateTinkerVertex(), EmptyOutEdges.instance());
    }

    public TinkerVertex getBaseVertex() {
        return this.tinkerVertex;
    }

    @Override
    public void compute(final Iterable<ObjectWritable> messages) {
        final VertexProgram vertexProgram = ((GiraphWorkerContext) this.getWorkerContext()).getVertexProgram();
        final GiraphMemory memory = ((GiraphWorkerContext) this.getWorkerContext()).getMemory();
        final GiraphMessenger messenger = ((GiraphWorkerContext) this.getWorkerContext()).getMessenger(this, messages);
        ///
        if (null == this.tinkerVertex) inflateTinkerVertex();
        if (null == this.wrappedVertex)
            this.wrappedVertex = ComputerDataStrategy.wrapVertex(this.tinkerVertex, vertexProgram);
        ///////////
        if (!(Boolean) ((RuleWritable) this.getAggregatedValue(Constants.GREMLIN_HADOOP_HALT)).getObject())
            vertexProgram.execute(this.wrappedVertex, messenger, memory);  // TODO provide a wrapper around TinkerVertex for Edge and non-ComputeKeys manipulation
        else if (this.getConf().getBoolean(Constants.GREMLIN_HADOOP_DERIVE_MEMORY, false)) {
            final MapMemory mapMemory = new MapMemory();
            memory.asMap().forEach(mapMemory::set);
            mapMemory.setIteration(memory.getIteration() - 1);
            this.wrappedVertex.property(VertexProperty.Cardinality.single, Constants.MAP_MEMORY, mapMemory);  // TODO: this is a "computer key"
        }
    }

    ///////////////////////////////////////////////

    private Text deflateTinkerVertex() {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            KRYO_WRITER.writeGraph(bos, this.tinkerVertex.graph());
            bos.flush();
            bos.close();
            return new Text(bos.toByteArray());
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void inflateTinkerVertex() {
        try {
            final ByteArrayInputStream bis = new ByteArrayInputStream(this.getValue().getBytes());
            final TinkerGraph tinkerGraph = TinkerGraph.open();
            KRYO_READER.readGraph(bis, tinkerGraph);
            bis.close();
            this.tinkerVertex = (TinkerVertex) tinkerGraph.iterators().vertexIterator(tinkerGraph.variables().get(VERTEX_ID).get()).next();
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static final TinkerVertex generateTinkerVertexForm(final com.tinkerpop.gremlin.structure.Vertex otherVertex) {
        if (otherVertex instanceof TinkerVertex)
            return (TinkerVertex) otherVertex;
        else {
            try {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                KRYO_WRITER.writeVertex(bos, otherVertex, Direction.BOTH);
                bos.flush();
                bos.close();
                final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                final TinkerGraph tinkerGraph = TinkerGraph.open();
                final TinkerVertex tinkerVertex;
                final Function<DetachedVertex, com.tinkerpop.gremlin.structure.Vertex> vertexMaker = detachedVertex -> DetachedVertex.addTo(tinkerGraph, detachedVertex);
                final Function<DetachedEdge, Edge> edgeMaker = detachedEdge -> DetachedEdge.addTo(tinkerGraph, detachedEdge);
                try (InputStream in = new ByteArrayInputStream(bos.toByteArray())) {
                    tinkerVertex = (TinkerVertex) KRYO_READER.readVertex(in, Direction.BOTH, vertexMaker, edgeMaker);
                }
                bis.close();
                return tinkerVertex;
            } catch (final Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}
