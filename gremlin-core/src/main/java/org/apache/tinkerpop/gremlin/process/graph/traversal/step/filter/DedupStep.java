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
package com.tinkerpop.gremlin.process.graph.traversal.step.filter;

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.traversal.lambda.IdentityTraversal;
import com.tinkerpop.gremlin.process.traversal.step.Reducing;
import com.tinkerpop.gremlin.process.traversal.step.Reversible;
import com.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import com.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import com.tinkerpop.gremlin.process.traversal.util.TraversalUtil;
import com.tinkerpop.gremlin.process.traverser.TraverserRequirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class DedupStep<S> extends FilterStep<S> implements Reversible, Reducing<Set<Object>, S>, TraversalParent {

    private Traversal.Admin<S, Object> dedupTraversal = new IdentityTraversal<>();
    private Set<Object> duplicateSet = new HashSet<>();

    public DedupStep(final Traversal.Admin traversal) {
        super(traversal);
    }

    @Override
    protected boolean filter(final Traverser.Admin<S> traverser) {
        traverser.setBulk(1);
        return this.duplicateSet.add(TraversalUtil.apply(traverser, this.dedupTraversal));
    }


    @Override
    public List<Traversal<S, Object>> getLocalChildren() {
        return Collections.singletonList(this.dedupTraversal);
    }

    @Override
    public void addLocalChild(final Traversal.Admin dedupTraversal) {
        this.dedupTraversal = this.integrateChild(dedupTraversal, TYPICAL_LOCAL_OPERATIONS);
    }

    @Override
    public Reducer<Set<Object>, S> getReducer() {
        return new Reducer<>(HashSet::new, (set, start) -> {
            set.add(TraversalUtil.apply(start, this.dedupTraversal));
            return set;
        }, true);
    }

    @Override
    public DedupStep<S> clone() throws CloneNotSupportedException {
        final DedupStep<S> clone = (DedupStep<S>) super.clone();
        clone.duplicateSet = new HashSet<>();
        clone.dedupTraversal = clone.integrateChild(this.dedupTraversal.clone(), TYPICAL_LOCAL_OPERATIONS);
        return clone;
    }

    @Override
    public void reset() {
        super.reset();
        this.duplicateSet.clear();
    }

    @Override
    public String toString() {
        return TraversalHelper.makeStepString(this, this.dedupTraversal);
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.SIDE_EFFECTS);
    }
}
