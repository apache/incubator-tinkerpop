/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.Scoping;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.AndStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.ConjunctionStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.NotStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.WherePredicateStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.WhereTraversalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.ProfileStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.StartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ComputerAwareStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ReducingBarrierStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.ConjunctionStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class MatchStep<S, E> extends ComputerAwareStep<S, Map<String, E>> implements TraversalParent, Scoping {

    public static enum TraversalType {WHERE_PREDICATE, WHERE_TRAVERSAL, MATCH_TRAVERSAL}

    private List<Traversal.Admin<Object, Object>> matchTraversals = new ArrayList<>();
    private boolean first = true;
    private Set<String> matchStartLabels = new HashSet<>();
    private Set<String> matchEndLabels = new HashSet<>();
    private Set<String> scopeKeys = null;
    private final ConjunctionStep.Conjunction conjunction;
    private final String computedStartLabel;
    private MatchAlgorithm matchAlgorithm;
    private Class<? extends MatchAlgorithm> matchAlgorithmClass = CountMatchAlgorithm.class; // default is CountMatchAlgorithm (use MatchAlgorithmStrategy to change)

    private Set<List<Object>> dedups = null;
    private Set<String> dedupLabels = null;

    public MatchStep(final Traversal.Admin traversal, final ConjunctionStep.Conjunction conjunction, final Traversal... matchTraversals) {
        super(traversal);
        this.conjunction = conjunction;
        this.matchTraversals = (List) Stream.of(matchTraversals).map(Traversal::asAdmin).collect(Collectors.toList());
        this.matchTraversals.forEach(this::configureStartAndEndSteps); // recursively convert to MatchStep, MatchStartStep, or MatchEndStep
        this.matchTraversals.forEach(this::integrateChild);
        this.computedStartLabel = Helper.computeStartLabel(this.matchTraversals);
    }

    //////////////////
    private String pullOutVariableStartStepToParent(final WhereTraversalStep<?> whereStep) {
        return this.pullOutVariableStartStepToParent(new HashSet<>(), whereStep.getLocalChildren().get(0), true).size() != 1 ? null : pullOutVariableStartStepToParent(new HashSet<>(), whereStep.getLocalChildren().get(0), false).iterator().next();
    }

    private Set<String> pullOutVariableStartStepToParent(final Set<String> selectKeys, final Traversal.Admin<?, ?> traversal, boolean testRun) {
        final Step<?, ?> startStep = traversal.getStartStep();
        if (startStep instanceof WhereTraversalStep.WhereStartStep && !((WhereTraversalStep.WhereStartStep) startStep).getScopeKeys().isEmpty()) {
            selectKeys.addAll(((WhereTraversalStep.WhereStartStep<?>) startStep).getScopeKeys());
            if (!testRun) ((WhereTraversalStep.WhereStartStep) startStep).removeScopeKey();
        } else if (startStep instanceof ConjunctionStep || startStep instanceof NotStep) {
            ((TraversalParent) startStep).getLocalChildren().forEach(child -> this.pullOutVariableStartStepToParent(selectKeys, child, testRun));
        }
        return selectKeys;
    }
    //////////////////

    private void configureStartAndEndSteps(final Traversal.Admin<?, ?> matchTraversal) {
        ConjunctionStrategy.instance().apply(matchTraversal);
        // START STEP to MatchStep OR MatchStartStep
        final Step<?, ?> startStep = matchTraversal.getStartStep();
        if (startStep instanceof ConjunctionStep) {
            final MatchStep matchStep = new MatchStep(matchTraversal,
                    startStep instanceof AndStep ? ConjunctionStep.Conjunction.AND : ConjunctionStep.Conjunction.OR,
                    ((ConjunctionStep<?>) startStep).getLocalChildren().toArray(new Traversal[((ConjunctionStep<?>) startStep).getLocalChildren().size()]));
            TraversalHelper.replaceStep(startStep, matchStep, matchTraversal);
            this.matchStartLabels.addAll(matchStep.matchStartLabels);
            this.matchEndLabels.addAll(matchStep.matchEndLabels);
        } else if (startStep instanceof NotStep) {
            final DefaultTraversal notTraversal = new DefaultTraversal<>();
            TraversalHelper.removeToTraversal(startStep, startStep.getNextStep(), notTraversal);
            matchTraversal.addStep(0, new WhereTraversalStep<>(matchTraversal, notTraversal));
            this.configureStartAndEndSteps(matchTraversal);
        } else if (StartStep.isVariableStartStep(startStep)) {
            final String label = startStep.getLabels().iterator().next();
            this.matchStartLabels.add(label);
            TraversalHelper.replaceStep((Step) matchTraversal.getStartStep(), new MatchStartStep(matchTraversal, label), matchTraversal);
        } else if (startStep instanceof WhereTraversalStep) {  // necessary for GraphComputer so the projection is not select'd from a path
            final WhereTraversalStep<?> whereStep = (WhereTraversalStep<?>) startStep;
            TraversalHelper.insertBeforeStep(new MatchStartStep(matchTraversal, this.pullOutVariableStartStepToParent(whereStep)), (Step) whereStep, matchTraversal);             // where(as('a').out()) -> as('a').where(out())
        } else if (startStep instanceof WherePredicateStep) {  // necessary for GraphComputer so the projection is not select'd from a path
            final WherePredicateStep<?> whereStep = (WherePredicateStep<?>) startStep;
            TraversalHelper.insertBeforeStep(new MatchStartStep(matchTraversal, whereStep.getStartKey().orElse(null)), (Step) whereStep, matchTraversal);   // where('a',eq('b')) --> as('a').where(eq('b'))
            whereStep.removeStartKey();
        } else {
            throw new IllegalArgumentException("All match()-traversals must have a single start label (i.e. variable): " + matchTraversal);
        }
        // END STEP to MatchEndStep
        final Step<?, ?> endStep = matchTraversal.getEndStep();
        if (endStep.getLabels().size() > 1)
            throw new IllegalArgumentException("The end step of a match()-traversal can have at most one label: " + endStep);
        final String label = endStep.getLabels().size() == 0 ? null : endStep.getLabels().iterator().next();
        if (null != label) endStep.removeLabel(label);
        final Step<?, ?> matchEndStep = new MatchEndStep(matchTraversal, label);
        if (null != label) this.matchEndLabels.add(label);
        matchTraversal.asAdmin().addStep(matchEndStep);

        // this turns barrier computations into locally computable traversals
        if (!TraversalHelper.getStepsOfAssignableClass(ReducingBarrierStep.class, matchTraversal).isEmpty()) {
            final Traversal.Admin newTraversal = new DefaultTraversal<>();
            TraversalHelper.removeToTraversal(matchTraversal.getStartStep().getNextStep(), matchTraversal.getEndStep(), newTraversal);
            TraversalHelper.insertAfterStep(new TraversalFlatMapStep<>(matchTraversal, newTraversal), matchTraversal.getStartStep(), matchTraversal);
        }
    }

    public ConjunctionStep.Conjunction getConjunction() {
        return this.conjunction;
    }

    public void addGlobalChild(final Traversal.Admin<?, ?> globalChildTraversal) {
        this.configureStartAndEndSteps(globalChildTraversal);
        this.matchTraversals.add(this.integrateChild(globalChildTraversal));
    }

    @Override
    public void removeGlobalChild(final Traversal.Admin<?, ?> globalChildTraversal) {
        this.matchTraversals.remove(globalChildTraversal);
    }

    @Override
    public List<Traversal.Admin<Object, Object>> getGlobalChildren() {
        return Collections.unmodifiableList(this.matchTraversals);
    }

    @Override
    public Set<String> getScopeKeys() {
        if (null == this.scopeKeys) {
            this.scopeKeys = new HashSet<>();
            this.matchTraversals.forEach(traversal -> {
                if (traversal.getStartStep() instanceof Scoping)
                    this.scopeKeys.addAll(((Scoping) traversal.getStartStep()).getScopeKeys());
                if (traversal.getEndStep() instanceof Scoping)
                    this.scopeKeys.addAll(((Scoping) traversal.getEndStep()).getScopeKeys());
            });
            this.scopeKeys.removeAll(this.matchEndLabels);
            this.scopeKeys.remove(this.computedStartLabel);
            this.scopeKeys = Collections.unmodifiableSet(this.scopeKeys);
        }
        return this.scopeKeys;
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.dedupLabels, this.conjunction, this.matchTraversals);
    }

    @Override
    public void reset() {
        super.reset();
        this.first = true;
    }

    public void setMatchAlgorithm(final Class<? extends MatchAlgorithm> matchAlgorithmClass) {
        this.matchAlgorithmClass = matchAlgorithmClass;
    }

    public MatchAlgorithm getMatchAlgorithm() {
        return this.matchAlgorithm;
    }

    @Override
    public MatchStep<S, E> clone() {
        final MatchStep<S, E> clone = (MatchStep<S, E>) super.clone();
        clone.matchTraversals = new ArrayList<>();
        for (final Traversal.Admin<Object, Object> traversal : this.matchTraversals) {
            clone.matchTraversals.add(clone.integrateChild(traversal.clone()));
        }
        if (this.dedups != null) clone.dedups = new HashSet<>();
        clone.initializeMatchAlgorithm();
        return clone;
    }

    public void setDedupLabels(final Set<String> labels) {
        if (!labels.isEmpty()) {
            this.dedups = new HashSet<>();
            this.dedupLabels = new HashSet<>(labels);
        }
    }

    /*public boolean isDeduping() {
        return this.dedupLabels != null;
    }*/

    private boolean isDuplicate(final Traverser<S> traverser) {
        if (null == this.dedups)
            return false;
        for (final String label : this.dedupLabels) {
            if (!traverser.path().hasLabel(label))
                return false;
        }
        final List<Object> objects = new ArrayList<>(this.dedupLabels.size());
        for (final String label : this.dedupLabels) {
            objects.add(traverser.path().get(Pop.last, label));
        }
        return this.dedups.contains(objects);
    }

    private boolean hasMatched(final ConjunctionStep.Conjunction conjunction, final Traverser<S> traverser) {
        final Path path = traverser.path();
        int counter = 0;
        boolean matched = false;
        for (final Traversal.Admin<Object, Object> matchTraversal : this.matchTraversals) {
            if (path.hasLabel(matchTraversal.getStartStep().getId())) {
                if (conjunction == ConjunctionStep.Conjunction.OR) {
                    matched = true;
                    break;
                }
                counter++;
            }
        }
        if (!matched)
            matched = this.matchTraversals.size() == counter;
        if (matched && this.dedupLabels != null) {
            final List<Object> objects = new ArrayList<>(this.dedupLabels.size());
            for (final String label : this.dedupLabels) {
                objects.add(traverser.path().get(Pop.last, label));
            }
            this.dedups.add(objects);
        }
        return matched;
    }

    private Map<String, E> getBindings(final Traverser<S> traverser) {
        final Map<String, E> bindings = new HashMap<>();
        traverser.path().forEach((object, labels) -> {
            for (final String label : labels) {
                if (this.matchStartLabels.contains(label) || this.matchEndLabels.contains(label))
                    bindings.put(label, (E) object);
            }
        });
        return bindings;
    }

    private void initializeMatchAlgorithm() {
        try {
            this.matchAlgorithm = this.matchAlgorithmClass.getConstructor().newInstance();
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        this.matchAlgorithm.initialize(this.matchTraversals);
    }

    @Override
    protected Iterator<Traverser<Map<String, E>>> standardAlgorithm() throws NoSuchElementException {
        while (true) {
            Traverser.Admin traverser = null;
            if (this.first) {
                this.first = false;
                this.initializeMatchAlgorithm();
            } else {
                for (final Traversal.Admin<?, ?> matchTraversal : this.matchTraversals) {
                    if (matchTraversal.hasNext()) {
                        traverser = matchTraversal.getEndStep().next().asAdmin();
                        break;
                    }
                }
            }
            if (null == traverser) {
                traverser = this.starts.next();
                final Path path = traverser.path();
                if (!this.matchStartLabels.stream().filter(path::hasLabel).findAny().isPresent())
                    path.addLabel(this.computedStartLabel); // if the traverser doesn't have a legal start, then provide it the pre-computed one
                path.addLabel(this.getId()); // so the traverser never returns to this branch ever again
            }
            ///
            if (!this.isDuplicate(traverser)) {
                if (hasMatched(this.conjunction, traverser))
                    return IteratorUtils.of(traverser.split(this.getBindings(traverser), this));

                if (this.conjunction == ConjunctionStep.Conjunction.AND) {
                    this.getMatchAlgorithm().apply(traverser).addStart(traverser); // determine which sub-pattern the traverser should try next
                } else {  // OR
                    for (final Traversal.Admin<?, ?> matchTraversal : this.matchTraversals) {
                        matchTraversal.addStart(traverser.split());
                    }
                }
            }
        }
    }

    @Override
    protected Iterator<Traverser<Map<String, E>>> computerAlgorithm() throws NoSuchElementException {
        while (true) {
            final Traverser.Admin traverser = this.starts.next();
            final Path path = traverser.path();
            if (!this.matchStartLabels.stream().filter(path::hasLabel).findAny().isPresent())
                path.addLabel(this.computedStartLabel); // if the traverser doesn't have a legal start, then provide it the pre-computed one
            if (!path.hasLabel(this.getId()))
                path.addLabel(this.getId()); // so the traverser never returns to this branch ever again
            ///
            if (!this.isDuplicate(traverser)) {
                if (hasMatched(this.conjunction, traverser)) {
                    traverser.setStepId(this.getNextStep().getId());
                    return IteratorUtils.of(traverser.split(this.getBindings(traverser), this));
                }
                if (this.conjunction == ConjunctionStep.Conjunction.AND) {
                    final Traversal.Admin<Object, Object> matchTraversal = this.getMatchAlgorithm().apply(traverser); // determine which sub-pattern the traverser should try next
                    traverser.setStepId(matchTraversal.getStartStep().getId()); // go down the traversal match sub-pattern
                    return IteratorUtils.of(traverser);
                } else { // OR
                    final List<Traverser<Map<String, E>>> traversers = new ArrayList<>(this.matchTraversals.size());
                    this.matchTraversals.forEach(matchTraversal -> {
                        final Traverser.Admin split = traverser.split();
                        split.setStepId(matchTraversal.getStartStep().getId());
                        traversers.add(split);
                    });
                    return traversers.iterator();
                }
            }
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode() ^ this.conjunction.hashCode();
        for (final Traversal t : this.matchTraversals) {
            result ^= t.hashCode();
        }
        return result;
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.PATH, TraverserRequirement.SIDE_EFFECTS);
    }

    //////////////////////////////

    public static class MatchStartStep extends AbstractStep<Object, Object> implements Scoping {

        private final String selectKey;
        private Set<String> scopeKeys = null;

        public MatchStartStep(final Traversal.Admin traversal, final String selectKey) {
            super(traversal);
            this.selectKey = selectKey;
        }

        @Override
        protected Traverser<Object> processNextStart() throws NoSuchElementException {
            final Traverser.Admin<Object> traverser = this.starts.next();
            traverser.path().addLabel(this.getId());
            ((MatchStep<?, ?>) this.getTraversal().getParent()).getMatchAlgorithm().recordStart(traverser, this.getTraversal());
            // TODO: sideEffect check?
            return null == this.selectKey ? traverser : traverser.split(traverser.path().get(Pop.last, this.selectKey), this);
        }

        @Override
        public String toString() {
            return StringFactory.stepString(this, this.selectKey);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            if (null != this.selectKey)
                result ^= this.selectKey.hashCode();
            return result;
        }

        public Optional<String> getSelectKey() {
            return Optional.ofNullable(this.selectKey);
        }

        @Override
        public Set<String> getScopeKeys() {
            if (null == this.scopeKeys) {
                this.scopeKeys = new HashSet<>();
                if (null != this.selectKey)
                    this.scopeKeys.add(this.selectKey);
                if (this.getNextStep() instanceof Scoping)
                    this.scopeKeys.addAll(((Scoping) this.getNextStep()).getScopeKeys());
                this.scopeKeys = Collections.unmodifiableSet(this.scopeKeys);
            }
            return this.scopeKeys;
        }
    }

    public static class MatchEndStep extends EndStep<Object> {

        private final String matchKey;

        public MatchEndStep(final Traversal.Admin traversal, final String matchKey) {
            super(traversal);
            this.matchKey = matchKey;
        }

        @Override
        protected Traverser<Object> processNextStart() throws NoSuchElementException {
            while (true) {
                final Traverser.Admin traverser = this.starts.next();
                // no end label
                if (null == this.matchKey) {
                    if (this.traverserStepIdSetByChild)
                        traverser.setStepId(((MatchStep<?, ?>) this.getTraversal().getParent()).getId());
                    ((MatchStep<?, ?>) this.getTraversal().getParent()).getMatchAlgorithm().recordEnd(traverser, this.getTraversal());
                    return traverser;
                }
                // TODO: sideEffect check?
                // path check
                final Path path = traverser.path();
                if (!path.hasLabel(this.matchKey) || traverser.get().equals(path.get(Pop.last, this.matchKey))) {
                    if (this.traverserStepIdSetByChild)
                        traverser.setStepId(((MatchStep<?, ?>) this.getTraversal().getParent()).getId());
                    traverser.path().addLabel(this.matchKey);
                    ((MatchStep<?, ?>) this.getTraversal().getParent()).getMatchAlgorithm().recordEnd(traverser, this.getTraversal());
                    return traverser;
                }
            }
        }

        public Optional<String> getMatchKey() {
            return Optional.ofNullable(this.matchKey);
        }

        @Override
        public String toString() {
            return StringFactory.stepString(this, this.matchKey);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            if (null != this.matchKey)
                result ^= this.matchKey.hashCode();
            return result;
        }
    }


    //////////////////////////////

    public static final class Helper {
        private Helper() {
        }

        public static Optional<String> getEndLabel(final Traversal.Admin<Object, Object> traversal) {
            final Step<?, ?> endStep = traversal.getEndStep();
            return endStep instanceof ProfileStep ?           // TOTAL HACK
                    ((MatchEndStep) endStep.getPreviousStep()).getMatchKey() :
                    ((MatchEndStep) endStep).getMatchKey();
        }

        public static Set<String> getStartLabels(final Traversal.Admin<Object, Object> traversal) {
            return ((Scoping) traversal.getStartStep()).getScopeKeys();
        }

        public static boolean hasStartLabels(final Traverser.Admin<Object> traverser, final Traversal.Admin<Object, Object> traversal) {
            return !Helper.getStartLabels(traversal).stream().filter(label -> !traverser.path().hasLabel(label)).findAny().isPresent();
        }

        public static boolean hasEndLabel(final Traverser.Admin<Object> traverser, final Traversal.Admin<Object, Object> traversal) {
            final Optional<String> endLabel = Helper.getEndLabel(traversal);
            return endLabel.isPresent() && traverser.path().hasLabel(endLabel.get()); // TODO: !isPresent?
        }

        public static boolean hasExecutedTraversal(final Traverser.Admin<Object> traverser, final Traversal.Admin<Object, Object> traversal) {
            return traverser.path().hasLabel(traversal.getStartStep().getId());
        }

        public static TraversalType getTraversalType(final Traversal.Admin<Object, Object> traversal) {
            final Step<?, ?> nextStep = traversal.getStartStep().getNextStep();
            if (nextStep instanceof WherePredicateStep)
                return TraversalType.WHERE_PREDICATE;
            else if (nextStep instanceof WhereTraversalStep)
                return TraversalType.WHERE_TRAVERSAL;
            else
                return TraversalType.MATCH_TRAVERSAL;
        }

        public static String computeStartLabel(final List<Traversal.Admin<Object, Object>> traversals) {
            final List<String> sort = new ArrayList<>();
            for (final Traversal.Admin<Object, Object> traversal : traversals) {
                Helper.getStartLabels(traversal).stream().filter(startLabel -> !sort.contains(startLabel)).forEach(sort::add);
                Helper.getEndLabel(traversal).ifPresent(endLabel -> {
                    if (!sort.contains(endLabel))
                        sort.add(endLabel);
                });
            }
            Collections.sort(sort, (a, b) -> {
                for (final Traversal.Admin<Object, Object> traversal : traversals) {
                    final Optional<String> endLabel = Helper.getEndLabel(traversal);
                    if (endLabel.isPresent()) {
                        final Set<String> startLabels = Helper.getStartLabels(traversal);
                        if (a.equals(endLabel.get()) && startLabels.contains(b))
                            return 1;
                        else if (b.equals(endLabel.get()) && startLabels.contains(a))
                            return -1;
                    }
                }
                return 0;
            });
            //System.out.println(sort);
            return sort.get(0);
        }
    }


    //////////////////////////////

    public interface MatchAlgorithm extends Function<Traverser.Admin<Object>, Traversal.Admin<Object, Object>>, Serializable {


        public static Function<List<Traversal.Admin<Object, Object>>, IllegalStateException> UNMATCHABLE_PATTERN = traversals -> new IllegalStateException("The provided match pattern is unsolvable: " + traversals);


        public void initialize(final List<Traversal.Admin<Object, Object>> traversals);

        public default void recordStart(final Traverser.Admin<Object> traverser, final Traversal.Admin<Object, Object> traversal) {

        }

        public default void recordEnd(final Traverser.Admin<Object> traverser, final Traversal.Admin<Object, Object> traversal) {

        }
    }

    public static class GreedyMatchAlgorithm implements MatchAlgorithm {

        private List<Traversal.Admin<Object, Object>> traversals;

        @Override
        public void initialize(final List<Traversal.Admin<Object, Object>> traversals) {
            this.traversals = traversals;
        }

        @Override
        public Traversal.Admin<Object, Object> apply(final Traverser.Admin<Object> traverser) {
            for (final Traversal.Admin<Object, Object> traversal : this.traversals) {
                if (!Helper.hasExecutedTraversal(traverser, traversal) && Helper.hasStartLabels(traverser, traversal))
                    return traversal;
            }
            throw UNMATCHABLE_PATTERN.apply(this.traversals);
        }
    }

    public static class CountMatchAlgorithm implements MatchAlgorithm {

        protected List<Bundle> bundles;
        protected int counter = 0;

        @Override
        public void initialize(final List<Traversal.Admin<Object, Object>> traversals) {
            this.bundles = traversals.stream().map(Bundle::new).collect(Collectors.toList());
        }

        @Override
        public Traversal.Admin<Object, Object> apply(final Traverser.Admin<Object> traverser) {
            Bundle startLabelsBundle = null;
            for (final Bundle bundle : this.bundles) {
                if (!Helper.hasExecutedTraversal(traverser, bundle.traversal) && Helper.hasStartLabels(traverser, bundle.traversal)) {
                    if (bundle.traversalType != TraversalType.MATCH_TRAVERSAL || Helper.hasEndLabel(traverser, bundle.traversal))
                        return bundle.traversal;
                    else if (null == startLabelsBundle)
                        startLabelsBundle = bundle;
                }
            }
            if (null != startLabelsBundle) return startLabelsBundle.traversal;
            throw UNMATCHABLE_PATTERN.apply(this.bundles.stream().map(record -> record.traversal).collect(Collectors.toList()));
        }

        @Override
        public void recordStart(final Traverser.Admin<Object> traverser, final Traversal.Admin<Object, Object> traversal) {
            this.getBundle(traversal).startsCount++;
        }

        @Override
        public void recordEnd(final Traverser.Admin<Object> traverser, final Traversal.Admin<Object, Object> traversal) {
            this.getBundle(traversal).incrementEndCount();
            if (this.counter < 200 || this.counter % 250 == 0) // aggressively sort for the first 200 results -- after that, sort every 250
                Collections.sort(this.bundles, Comparator.<Bundle>comparingInt(b -> b.traversalType.ordinal()).thenComparingDouble(b -> b.multiplicity));
            this.counter++;
        }

        protected Bundle getBundle(final Traversal.Admin<Object, Object> traversal) {
            for (final Bundle bundle : this.bundles) {
                if (bundle.traversal == traversal)
                    return bundle;
            }
            throw new IllegalStateException("No equivalent traversal could be found in " + CountMatchAlgorithm.class.getSimpleName() + ": " + traversal);
        }

        ///////////

        public class Bundle {
            public Traversal.Admin<Object, Object> traversal;
            public TraversalType traversalType;
            public long startsCount;
            public long endsCount;
            public double multiplicity;

            public Bundle(final Traversal.Admin<Object, Object> traversal) {
                this.traversal = traversal;
                this.traversalType = Helper.getTraversalType(traversal);
                this.startsCount = 0l;
                this.endsCount = 0l;
                this.multiplicity = 0.0d;
            }

            public final void incrementEndCount() {
                this.multiplicity = (double) ++this.endsCount / (double) this.startsCount;
            }
        }
    }
}
