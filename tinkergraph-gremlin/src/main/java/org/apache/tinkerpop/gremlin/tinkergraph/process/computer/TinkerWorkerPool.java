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
package org.apache.tinkerpop.gremlin.tinkergraph.process.computer;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.tinkerpop.gremlin.process.computer.MapReduce;
import org.apache.tinkerpop.gremlin.process.computer.Memory;
import org.apache.tinkerpop.gremlin.process.computer.VertexProgram;
import org.apache.tinkerpop.gremlin.process.computer.util.MapReducePool;
import org.apache.tinkerpop.gremlin.process.computer.util.VertexProgramPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class TinkerWorkerPool implements AutoCloseable {

    private static final BasicThreadFactory threadFactoryWorker = new BasicThreadFactory.Builder().namingPattern("tinker-worker-%d").build();

    private final int numberOfWorkers;
    private final ExecutorService workerPool;

    private VertexProgramPool vertexProgramPool;
    private MapReducePool mapReducePool;

    public TinkerWorkerPool(final int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
        workerPool = Executors.newFixedThreadPool(numberOfWorkers, threadFactoryWorker);
    }

    public void setVertexProgram(final VertexProgram vertexProgram) {
        this.vertexProgramPool = new VertexProgramPool(vertexProgram, this.numberOfWorkers);
    }

    public void setMapReduce(final MapReduce mapReduce) {
        this.mapReducePool = new MapReducePool(mapReduce, this.numberOfWorkers);
    }

    public void executeVertexProgram(final Consumer<VertexProgram> worker) {
        try {
            this.workerPool.submit(() -> {
                final VertexProgram vp = this.vertexProgramPool.take();
                worker.accept(vp);
                this.vertexProgramPool.offer(vp);
            }).get();
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void executeMapReduce(final Consumer<MapReduce> worker) {
        try {
            this.workerPool.submit(() -> {
                final MapReduce mr = this.mapReducePool.take();
                worker.accept(mr);
                this.mapReducePool.offer(mr);
            }).get();
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        workerPool.shutdown();
    }
}