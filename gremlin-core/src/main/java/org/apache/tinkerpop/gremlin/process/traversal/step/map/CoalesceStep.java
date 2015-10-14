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
package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ComputerAwareStep;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.EmptyIterator;

import java.util.*;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public final class CoalesceStep<S, E> extends ComputerAwareStep<S, E> implements TraversalParent {

    private List<Traversal.Admin<S, E>> coalesceTraversals;

    @SafeVarargs
    public CoalesceStep(final Traversal.Admin traversal, final Traversal.Admin<S, E>... coalesceTraversals) {
        super(traversal);
        this.coalesceTraversals = Arrays.asList(coalesceTraversals);
        for (final Traversal.Admin<S, ?> conjunctionTraversal : this.coalesceTraversals) {
            conjunctionTraversal.addStep(new EndStep(conjunctionTraversal));
            this.integrateChild(conjunctionTraversal);
        }
    }

    @Override
    protected Iterator<Traverser<E>> standardAlgorithm() {
        final Traverser.Admin<S> start = this.starts.next();
        for (final Traversal.Admin<S, E> coalesceTraversal : this.coalesceTraversals) {
            coalesceTraversal.reset();
            coalesceTraversal.addStart(start.split());
            if (coalesceTraversal.getEndStep().hasNext())
                return coalesceTraversal.getEndStep();
        }
        return EmptyIterator.instance();
    }

    @Override
    protected Iterator<Traverser<E>> computerAlgorithm() {
        // TODO: How to stop after the first successful option?
        final List<Traverser<E>> ends = new ArrayList<>();
        final Traverser.Admin<S> start = this.starts.next();
        this.coalesceTraversals.forEach(
            coalesceTraversal -> {
                final Traverser.Admin<E> split = (Traverser.Admin<E>) start.split();
                split.setStepId(coalesceTraversal.getStartStep().getId());
                ends.add(split);
            });
        return ends.iterator();
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements();
    }

    @Override
    public List<Traversal.Admin<S, E>> getGlobalChildren() {
        return Collections.unmodifiableList(this.coalesceTraversals);
    }

    @Override
    public CoalesceStep<S, E> clone() {
        final CoalesceStep<S, E> clone = (CoalesceStep<S, E>) super.clone();
        clone.coalesceTraversals = new ArrayList<>();
        for (final Traversal.Admin<S, ?> conjunctionTraversal : this.coalesceTraversals) {
            clone.coalesceTraversals.add(clone.integrateChild(conjunctionTraversal.clone()));
        }
        return clone;
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.coalesceTraversals);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode(), i = 0;
        for (final Traversal.Admin<S, E> traversal : this.coalesceTraversals) {
            result ^= Integer.rotateLeft(traversal.hashCode(), i++);
        }
        return result;
    }
}
