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
package com.apache.tinkerpop.gremlin.process.graph.traversal.step.branch;

import com.apache.tinkerpop.gremlin.process.FastNoSuchElementException;
import com.apache.tinkerpop.gremlin.process.Traversal;
import com.apache.tinkerpop.gremlin.process.Traverser;
import com.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import com.apache.tinkerpop.gremlin.process.traversal.step.AbstractStep;
import com.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import com.apache.tinkerpop.gremlin.process.traverser.TraverserRequirement;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class LocalStep<S, E> extends AbstractStep<S, E> implements TraversalParent {

    private Traversal.Admin<S, E> localTraversal;
    private boolean first = true;

    public LocalStep(final Traversal.Admin traversal, final Traversal.Admin<S, E> localTraversal) {
        super(traversal);
        this.integrateChild(this.localTraversal = localTraversal, TYPICAL_GLOBAL_OPERATIONS);
    }

    @Override
    public LocalStep<S, E> clone() throws CloneNotSupportedException {
        final LocalStep<S, E> clone = (LocalStep<S, E>) super.clone();
        clone.localTraversal = clone.integrateChild(this.localTraversal.clone(), TYPICAL_GLOBAL_OPERATIONS);
        clone.first = true;
        return clone;
    }

    @Override
    public String toString() {
        return TraversalHelper.makeStepString(this, this.localTraversal);
    }

    @Override
    public List<Traversal.Admin<S, E>> getLocalChildren() {
        return Collections.singletonList(this.localTraversal);
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.localTraversal.getTraverserRequirements();
    }

    @Override
    protected Traverser<E> processNextStart() throws NoSuchElementException {
        if (this.first) {
            this.first = false;
            this.localTraversal.addStart(this.starts.next());
        }
        while (true) {
            if (this.localTraversal.hasNext())
                return this.localTraversal.getEndStep().next();
            else if (this.starts.hasNext()) {
                this.localTraversal.reset();
                this.localTraversal.addStart(this.starts.next());
            } else {
                throw FastNoSuchElementException.instance();
            }
        }
    }
}
