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
package org.apache.tinkerpop.gremlin.process.traversal.step.filter;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.Scoping;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalP;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalUtil;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class WhereStep<S> extends FilterStep<S> implements TraversalParent, Scoping {

    protected P<Object> predicate;
    protected final Set<String> startKeys = new HashSet<>();
    protected final Set<String> endKeys = new HashSet<>();
    protected String startKey;
    protected String endKey;
    protected Scope scope;
    protected boolean multiKeyedTraversal;


    public WhereStep(final Traversal.Admin traversal, final Scope scope, final Optional<String> startKey, final P<?> predicate) {
        super(traversal);
        this.scope = scope;
        this.predicate = (P) predicate;
        if (!this.predicate.getTraversals().isEmpty()) {
            final Traversal.Admin<?, ?> whereTraversal = predicate.getTraversals().get(0);
            if (whereTraversal.getStartStep().getLabels().size() > 1 || whereTraversal.getEndStep().getLabels().size() > 1) {
                this.multiKeyedTraversal = true;
                this.startKeys.addAll(whereTraversal.getStartStep().getLabels());
                this.endKeys.addAll(whereTraversal.getEndStep().getLabels());
            } else {
                this.multiKeyedTraversal = false;
                this.startKey = whereTraversal.getStartStep().getLabels().isEmpty() ? null : whereTraversal.getStartStep().getLabels().iterator().next();
                this.endKey = whereTraversal.getEndStep().getLabels().isEmpty() ? null : whereTraversal.getEndStep().getLabels().iterator().next();
            }
            this.predicate.getTraversals().forEach(this::integrateChild);
        } else {
            this.multiKeyedTraversal = false;
            this.startKey = startKey.orElse(null);
            this.endKey = (String) (this.predicate.getValue() instanceof Collection ? ((Collection) this.predicate.getValue()).iterator().next() : this.predicate.getValue());
        }
    }

    public WhereStep(final Traversal.Admin traversal, final Scope scope, final P<?> predicate) {
        this(traversal, scope, Optional.empty(), predicate);
    }

    @Override
    protected boolean filter(final Traverser.Admin<S> traverser) {
        return this.multiKeyedTraversal ? this.doMultiKeyFilter(traverser) : this.doSingleKeyFilter(traverser);
    }

    @Override
    public List<Traversal.Admin<Object, Object>> getLocalChildren() {
        return this.predicate.getTraversals();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.scope, this.multiKeyedTraversal ? this.startKeys : this.startKey, this.predicate);
    }

    @Override
    public Set<String> getScopeKeys() {
        final Set<String> keys = new HashSet<>();
        if(this.multiKeyedTraversal) {
            keys.addAll(this.startKeys);
            keys.addAll(this.endKeys);
        } else {
            if (null != this.startKey)
                keys.add(this.startKey);
            if (null != this.endKey)
                keys.add(this.endKey);
        }
        return keys;

    }

    @Override
    public WhereStep<S> clone() {
        final WhereStep<S> clone = (WhereStep<S>) super.clone();
        clone.predicate = this.predicate.clone();
        clone.getLocalChildren().forEach(clone::integrateChild);
        return clone;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode() ^ this.scope.hashCode() ^ predicate.hashCode();
        if (this.startKey != null) result ^= this.startKey.hashCode();
        return result;
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(Scope.local == this.scope || this.noStartAndEndKeys() ?
                new TraverserRequirement[]{TraverserRequirement.OBJECT, TraverserRequirement.SIDE_EFFECTS} :
                new TraverserRequirement[]{TraverserRequirement.OBJECT, TraverserRequirement.PATH, TraverserRequirement.SIDE_EFFECTS});
    }

    @Override
    public void setScope(final Scope scope) {
        this.scope = scope;
    }

    @Override
    public Scope getScope() {
        return this.scope;
    }

    @Override
    public Scope recommendNextScope() {
        return this.scope;
    }

    private boolean noStartAndEndKeys() {
        return this.multiKeyedTraversal ? this.endKeys.isEmpty() && this.startKeys.isEmpty() : this.endKey == null && this.startKey == null;
    }

    private Object getStartObject(final Traverser.Admin<S> traverser) {
        return this.predicate instanceof TraversalP ? traverser : traverser.get();
    }

    private boolean doSingleKeyFilter(final Traverser.Admin<S> traverser) {
        if (this.noStartAndEndKeys()) {
            return this.predicate.getBiPredicate().test(getStartObject(traverser), null);
        } else {
            final Object startObject = null == this.startKey ? getStartObject(traverser) : this.getOptionalScopeValueByKey(this.startKey, traverser).orElse(null);
            if (null == startObject) return false;
            final Object endObject;
            if (null == this.endKey) {
                endObject = null;
            } else {
                endObject = this.getOptionalScopeValueByKey(this.endKey, traverser).orElse(null);
                if (null == endObject) return false;
            }
            return this.predicate.getBiPredicate().test(startObject, endObject);
        }
    }

    private boolean doMultiKeyFilter(final Traverser.Admin<S> traverser) {
        // TODO: getOptionalScopeValueByKey()
        final List<Object> startObjects = new ArrayList<>();
        final List<Object> endObjects = new ArrayList<>();

        if (this.startKeys.isEmpty())
            startObjects.add(traverser.get());
        else {
            for (final String startKey : this.startKeys) {
                startObjects.add(this.getScopeValueByKey(startKey, traverser));
            }
        }
        for (final String endKey : this.endKeys) {
            endObjects.add(this.getScopeValueByKey(endKey, traverser));
        }

        return this.predicate.getBiPredicate().test(new TraversalUtil.Multiple<>(startObjects), endObjects.isEmpty() ? null : new TraversalUtil.Multiple<>(endObjects));
    }
}
