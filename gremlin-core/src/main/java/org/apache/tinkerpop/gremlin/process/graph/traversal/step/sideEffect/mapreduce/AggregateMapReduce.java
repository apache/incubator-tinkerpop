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
package com.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.mapreduce;

import com.tinkerpop.gremlin.process.computer.KeyValue;
import com.tinkerpop.gremlin.process.computer.MapReduce;
import com.tinkerpop.gremlin.process.computer.traversal.TraversalVertexProgram;
import com.tinkerpop.gremlin.process.computer.traversal.VertexTraversalSideEffects;
import com.tinkerpop.gremlin.process.computer.util.StaticMapReduce;
import com.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.AggregateStep;
import com.tinkerpop.gremlin.process.util.BulkSet;
import com.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.configuration.Configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class AggregateMapReduce extends StaticMapReduce<MapReduce.NullObject, Object, MapReduce.NullObject, Object, Collection> {

    public static final String AGGREGATE_STEP_SIDE_EFFECT_KEY = "gremlin.aggregateStep.sideEffectKey";

    private String sideEffectKey;
    private Supplier<Collection> collectionSupplier;

    private AggregateMapReduce() {

    }

    public AggregateMapReduce(final AggregateStep step) {
        this.sideEffectKey = step.getSideEffectKey();
        this.collectionSupplier = step.getTraversal().asAdmin().getSideEffects().<Collection>getRegisteredSupplier(this.sideEffectKey).orElse(BulkSet::new);
    }

    @Override
    public void storeState(final Configuration configuration) {
        super.storeState(configuration);
        configuration.setProperty(AGGREGATE_STEP_SIDE_EFFECT_KEY, this.sideEffectKey);
    }

    @Override
    public void loadState(final Configuration configuration) {
        this.sideEffectKey = configuration.getString(AGGREGATE_STEP_SIDE_EFFECT_KEY);
        this.collectionSupplier = TraversalVertexProgram.getTraversalSupplier(configuration).get().getSideEffects().<Collection>getRegisteredSupplier(this.sideEffectKey).orElse(BulkSet::new);
    }

    @Override
    public boolean doStage(final Stage stage) {
        return stage.equals(Stage.MAP);
    }

    @Override
    public void map(final Vertex vertex, final MapEmitter<NullObject, Object> emitter) {
        VertexTraversalSideEffects.of(vertex).<Collection<?>>orElse(this.sideEffectKey, Collections.emptyList()).forEach(emitter::emit);
    }

    @Override
    public Collection generateFinalResult(final Iterator<KeyValue<NullObject, Object>> keyValues) {
        final Collection collection = this.collectionSupplier.get();
        keyValues.forEachRemaining(keyValue -> collection.add(keyValue.getValue()));
        return collection;
    }

    @Override
    public String getMemoryKey() {
        return this.sideEffectKey;
    }
}
