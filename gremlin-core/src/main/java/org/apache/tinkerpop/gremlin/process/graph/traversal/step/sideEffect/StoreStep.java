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
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.sideEffect;

import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.computer.MapReduce;
import org.apache.tinkerpop.gremlin.process.traversal.step.MapReducer;
import org.apache.tinkerpop.gremlin.process.traversal.step.Reversible;
import org.apache.tinkerpop.gremlin.process.graph.traversal.step.SideEffectCapable;
import org.apache.tinkerpop.gremlin.process.traversal.step.SideEffectRegistrar;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.mapreduce.StoreMapReduce;
import org.apache.tinkerpop.gremlin.process.traversal.lambda.IdentityTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalUtil;
import org.apache.tinkerpop.gremlin.process.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.util.BulkSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class StoreStep<S> extends SideEffectStep<S> implements SideEffectCapable, SideEffectRegistrar, Reversible, TraversalParent, MapReducer<MapReduce.NullObject, Object, MapReduce.NullObject, Object, Collection> {

    private Traversal.Admin<S, Object> storeTraversal = new IdentityTraversal<>();
    private String sideEffectKey;

    public StoreStep(final Traversal.Admin traversal, final String sideEffectKey) {
        super(traversal);
        this.sideEffectKey = sideEffectKey;
        StoreStep.generateConsumer(this);
    }

    @Override
    public void registerSideEffects() {
        if (null == this.sideEffectKey) this.sideEffectKey = this.getId();
        this.traversal.asAdmin().getSideEffects().registerSupplierIfAbsent(this.sideEffectKey, BulkSet::new);
    }

    @Override
    public String getSideEffectKey() {
        return this.sideEffectKey;
    }

    @Override
    public String toString() {
        return TraversalHelper.makeStepString(this, this.sideEffectKey, this.storeTraversal);
    }

    @Override
    public MapReduce<MapReduce.NullObject, Object, MapReduce.NullObject, Object, Collection> getMapReduce() {
        return new StoreMapReduce(this);
    }

    @Override
    public List<Traversal.Admin<S, Object>> getLocalChildren() {
        return Collections.singletonList(this.storeTraversal);
    }

    @Override
    public void addLocalChild(final Traversal.Admin<?, ?> storeTraversal) {
        this.storeTraversal = this.integrateChild(storeTraversal, TYPICAL_LOCAL_OPERATIONS);
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.SIDE_EFFECTS, TraverserRequirement.BULK);
    }

    @Override
    public StoreStep<S> clone() throws CloneNotSupportedException {
        final StoreStep<S> clone = (StoreStep<S>) super.clone();
        clone.storeTraversal = clone.integrateChild(this.storeTraversal.clone(), TYPICAL_LOCAL_OPERATIONS);
        StoreStep.generateConsumer(clone);
        return clone;
    }

    /////////////////////////

    private static final <S> void generateConsumer(final StoreStep<S> storeStep) {
        storeStep.setConsumer(traverser -> TraversalHelper.addToCollection(
                traverser.sideEffects(storeStep.sideEffectKey),
                TraversalUtil.apply(traverser.asAdmin(), storeStep.storeTraversal),
                traverser.bulk()));
    }
}
