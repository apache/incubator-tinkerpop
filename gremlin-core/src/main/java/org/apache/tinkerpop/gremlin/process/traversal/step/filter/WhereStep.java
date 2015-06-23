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
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.Scoping;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.StartStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.ConjunctionStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.ConjunctionP;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalUtil;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class WhereStep<S> extends FilterStep<S> implements TraversalParent, Scoping {

    protected String startKey;
    protected List<String> selectKeys;
    protected P<Object> predicate;
    protected Traversal.Admin<?, ?> whereTraversal;
    protected Scope scope;
    protected final Set<String> scopeKeys = new HashSet<>();

    public WhereStep(final Traversal.Admin traversal, final Scope scope, final Optional<String> startKey, final P<String> predicate) {
        super(traversal);
        this.scope = scope;
        this.startKey = startKey.orElse(null);
        if (null != this.startKey)
            this.scopeKeys.add(this.startKey);
        this.predicate = (P) predicate;
        this.selectKeys = new ArrayList<>();
        this.whereTraversal = null;
        this.configurePredicates(this.predicate);
    }

    public WhereStep(final Traversal.Admin traversal, final Scope scope, final Traversal<?, ?> whereTraversal) {
        super(traversal);
        this.scope = scope;
        this.startKey = null;
        this.predicate = null;
        this.selectKeys = null;
        this.whereTraversal = whereTraversal.asAdmin();
        this.configureStartAndEndSteps(this.whereTraversal);
        if (this.scopeKeys.isEmpty())
            throw new IllegalArgumentException("A where()-traversal must have at least a start or end label (i.e. variable): " + whereTraversal);
        this.whereTraversal = this.integrateChild(this.whereTraversal);
    }

    private void configureStartAndEndSteps(final Traversal.Admin<?, ?> whereTraversal) {
        ConjunctionStrategy.instance().apply(whereTraversal);
        //// START STEP to WhereStartStep
        final Step<?, ?> startStep = whereTraversal.getStartStep();
        if (startStep instanceof ConjunctionStep || startStep instanceof NotStep) {       // for conjunction- and not-steps
            ((TraversalParent) startStep).getLocalChildren().forEach(this::configureStartAndEndSteps);
        } else if (startStep instanceof StartStep && ((StartStep) startStep).isVariableStartStep()) {  // as("a").out()... traversals
            final String label = startStep.getLabels().iterator().next();
            this.scopeKeys.add(label);
            TraversalHelper.replaceStep(startStep, new WhereStartStep(whereTraversal, label), whereTraversal);
        } else if (!whereTraversal.getEndStep().getLabels().isEmpty()) {                    // ...out().as("a") traversals
            TraversalHelper.insertBeforeStep(new WhereStartStep(whereTraversal, null), (Step) startStep, whereTraversal);
        }
        //// END STEP to WhereEndStep
        final Step<?, ?> endStep = whereTraversal.getEndStep();
        if (!endStep.getLabels().isEmpty()) {
            if (endStep.getLabels().size() > 1)
                throw new IllegalArgumentException("The end step of a where()-traversal can only have one label: " + endStep);
            final String label = endStep.getLabels().iterator().next();
            this.scopeKeys.add(label);
            endStep.removeLabel(label);
            whereTraversal.addStep(new WhereEndStep(whereTraversal, label));
        }
    }

    private void configurePredicates(final P<Object> predicate) {
        if (predicate instanceof ConjunctionP)
            ((ConjunctionP<Object>) predicate).getPredicates().forEach(this::configurePredicates);
        else {
            final String selectKey = (String) (predicate.getValue() instanceof Collection ? ((Collection) predicate.getValue()).iterator().next() : predicate.getValue()); // hack for within("x"))
            this.selectKeys.add(selectKey);
            this.scopeKeys.add(selectKey);
        }
    }

    private void setPredicateValues(final P<Object> predicate, final Traverser.Admin<S> traverser, final Iterator<String> selectKeysIterator) {
        if (predicate instanceof ConjunctionP)
            ((ConjunctionP<Object>) predicate).getPredicates().forEach(p -> this.setPredicateValues(p, traverser, selectKeysIterator));
        else
            predicate.setValue(this.getScopeValueByKey(Pop.last, selectKeysIterator.next(), traverser));
    }

    public Optional<P<?>> getPredicate() {
        return Optional.ofNullable(this.predicate);
    }

    public Optional<String> getStartKey() {
        return Optional.ofNullable(this.startKey);
    }

    public boolean isPredicateBased() {
        return this.predicate != null;
    }

    public boolean isTraversalBased() {
        return this.whereTraversal != null;
    }

    public void removeStartKey() {
        this.selectKeys.remove(this.startKey);
        this.startKey = null;
    }

    @Override
    protected boolean filter(final Traverser.Admin<S> traverser) {
        if (null != this.whereTraversal)
            return TraversalUtil.test((Traverser.Admin) traverser, this.whereTraversal);
        else {
            this.setPredicateValues(this.predicate, traverser, this.selectKeys.iterator());
            return this.predicate.test(null == this.startKey ? traverser.get() : this.getScopeValueByKey(Pop.last, this.startKey, traverser));
        }
    }

    @Override
    public List<Traversal.Admin<?, ?>> getLocalChildren() {
        return null == this.whereTraversal ? Collections.emptyList() : Collections.singletonList(this.whereTraversal);
    }

    @Override
    public String toString() {
        // TODO: revert the predicates to their string form?
        return StringFactory.stepString(this, this.scope, this.startKey, this.predicate, this.whereTraversal);
    }

    @Override
    public Set<String> getScopeKeys() {
        return Collections.unmodifiableSet(this.scopeKeys);
    }

    @Override
    public WhereStep<S> clone() {
        final WhereStep<S> clone = (WhereStep<S>) super.clone();
        if (null != this.predicate)
            clone.predicate = this.predicate.clone();
        else
            clone.whereTraversal = clone.integrateChild(this.whereTraversal.clone());
        return clone;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.scope.hashCode() ^ (null == this.startKey ? "null".hashCode() : this.startKey.hashCode()) ^ (null == this.predicate ? this.whereTraversal.hashCode() : this.predicate.hashCode());
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(Scope.local == this.scope ?
                new TraverserRequirement[]{TraverserRequirement.OBJECT, TraverserRequirement.SIDE_EFFECTS} :
                new TraverserRequirement[]{TraverserRequirement.PATH, TraverserRequirement.SIDE_EFFECTS});
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

    //////////////////////////////

    public static class WhereStartStep<S> extends MapStep<S, Object> implements Scoping {

        private String selectKey;
        private Scope scope = Scope.global;

        public WhereStartStep(final Traversal.Admin traversal, final String selectKey) {
            super(traversal);
            this.selectKey = selectKey;
        }

        @Override
        protected Object map(final Traverser.Admin<S> traverser) {
            if (this.getTraversal().getEndStep() instanceof WhereEndStep)
                ((WhereEndStep) this.getTraversal().getEndStep()).processStartTraverser(traverser);
            return null == this.selectKey ? traverser.get() : this.getScopeValueByKey(Pop.last, this.selectKey, traverser);
        }

        @Override
        public String toString() {
            return StringFactory.stepString(this, this.scope, this.selectKey);
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ this.scope.hashCode() ^ (null == this.selectKey ? "null".hashCode() : this.selectKey.hashCode());
        }

        @Override
        public Scope getScope() {
            return this.scope;
        }

        @Override
        public Scope recommendNextScope() {
            return this.scope;
        }

        @Override
        public void setScope(Scope scope) {
            this.scope = scope;
        }

        public void removeScopeKey() {
            this.selectKey = null;
        }

        @Override
        public Set<String> getScopeKeys() {
            return null == this.selectKey ? Collections.emptySet() : Collections.singleton(this.selectKey);
        }
    }

    public static class WhereEndStep extends FilterStep<Object> implements Scoping {

        private final String matchKey;
        private Object matchValue = null;
        private Scope scope = Scope.global;

        public WhereEndStep(final Traversal.Admin traversal, final String matchKey) {
            super(traversal);
            this.matchKey = matchKey;
        }

        public void processStartTraverser(final Traverser.Admin traverser) {
            if (null != this.matchKey)
                this.matchValue = this.getScopeValueByKey(Pop.last, this.matchKey, traverser);
        }

        @Override
        protected boolean filter(final Traverser.Admin<Object> traverser) {
            return null == this.matchKey || traverser.get().equals(this.matchValue);
        }

        @Override
        public String toString() {
            return StringFactory.stepString(this, this.scope, this.matchKey);
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ this.scope.hashCode() ^ (null == this.matchKey ? "null".hashCode() : this.matchKey.hashCode());
        }

        @Override
        public Scope getScope() {
            return this.scope;
        }

        @Override
        public Scope recommendNextScope() {
            return this.scope;
        }

        @Override
        public void setScope(Scope scope) {
            // this.scope = scope;
        }

        @Override
        public Set<String> getScopeKeys() {
            return null == this.matchKey ? Collections.emptySet() : Collections.singleton(this.matchKey);
        }
    }


    //////////////////////////////
}
