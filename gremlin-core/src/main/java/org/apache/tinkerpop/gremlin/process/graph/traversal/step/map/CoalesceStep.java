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
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.map;

import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.process.traverser.TraverserRequirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public final class CoalesceStep<S, E> extends FlatMapStep<S, E> implements TraversalParent {

    private List<Traversal.Admin<S, E>> coalesceTraversals;

    public CoalesceStep(final Traversal.Admin traversal, final Traversal.Admin<S, E>... coalesceTraversals) {
        super(traversal);
        this.coalesceTraversals = Arrays.asList(coalesceTraversals);
        for (final Traversal.Admin<S, ?> conjunctionTraversal : this.coalesceTraversals) {
            this.integrateChild(conjunctionTraversal, TYPICAL_LOCAL_OPERATIONS);
        }
        CoalesceStep.generateFunction(this);
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements();
    }

    @Override
    public List<Traversal.Admin<S, E>> getLocalChildren() {
        return Collections.unmodifiableList(this.coalesceTraversals);
    }

    @Override
    public CoalesceStep<S, E> clone() throws CloneNotSupportedException {
        final CoalesceStep<S, E> clone = (CoalesceStep<S, E>) super.clone();
        clone.coalesceTraversals = new ArrayList<>();
        for (final Traversal.Admin<S, ?> conjunctionTraversal : this.coalesceTraversals) {
            clone.coalesceTraversals.add(clone.integrateChild(conjunctionTraversal.clone(), TYPICAL_LOCAL_OPERATIONS));
        }
        CoalesceStep.generateFunction(clone);
        return clone;
    }

    @Override
    public String toString() {
        return TraversalHelper.makeStepString(this, this.coalesceTraversals);
    }

    //////////////////////////

    private static <S, E> void generateFunction(final CoalesceStep<S, E> coalesceStep) {
        coalesceStep.setFunction(traverser -> {
            for (final Traversal.Admin<S, E> coalesceTraversal : coalesceStep.coalesceTraversals) {
                coalesceTraversal.reset();
                coalesceTraversal.addStart(traverser.asAdmin().split());
                if (coalesceTraversal.hasNext())
                    return coalesceTraversal;
            }
            return Collections.emptyIterator();
        });
    }
}
