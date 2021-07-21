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
package org.apache.tinkerpop.gremlin.process.traversal.step.branch;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.step.Barrier;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ComputerAwareStep;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalUtil;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class RepeatStep<S> extends ComputerAwareStep<S, S> implements TraversalParent {

    private Traversal.Admin<S, S> repeatTraversal = null;
    private Traversal.Admin<S, ?> untilTraversal = null;
    private Traversal.Admin<S, ?> emitTraversal = null;
    private String loopName = null;
    public boolean untilFirst = false;
    public boolean emitFirst = false;

    public RepeatStep(final Traversal.Admin traversal) {
        super(traversal);
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        final Set<TraverserRequirement> requirements = this.getSelfAndChildRequirements(TraverserRequirement.BULK);
        if (requirements.contains(TraverserRequirement.SINGLE_LOOP))
            requirements.add(TraverserRequirement.NESTED_LOOP);
        requirements.add(TraverserRequirement.SINGLE_LOOP);
        return requirements;
    }

    @SuppressWarnings("unchecked")
    public void setRepeatTraversal(final Traversal.Admin<S, S> repeatTraversal) {
        if (null != this.repeatTraversal)
            throw new IllegalStateException("The repeat()-step already has its loop section declared: " + this);
        this.repeatTraversal = repeatTraversal; // .clone();
        this.repeatTraversal.addStep(new RepeatEndStep(this.repeatTraversal));
        this.integrateChild(this.repeatTraversal);
    }


    public void setLoopName(final String loopName) {
        this.loopName = loopName;
    }

    public void setUntilTraversal(final Traversal.Admin<S, ?> untilTraversal) {
        if (null != this.untilTraversal)
            throw new IllegalStateException("The repeat()-step already has its until()-modulator declared: " + this);
        if (null == this.repeatTraversal) this.untilFirst = true;
        this.untilTraversal = this.integrateChild(untilTraversal);
    }

    public Traversal.Admin<S, ?> getUntilTraversal() {
        return this.untilTraversal;
    }

    public void setEmitTraversal(final Traversal.Admin<S, ?> emitTraversal) {
        if (null != this.emitTraversal)
            throw new IllegalStateException("The repeat()-step already has its emit()-modulator declared: " + this);
        if (null == this.repeatTraversal) this.emitFirst = true;
        this.emitTraversal = this.integrateChild(emitTraversal);
    }

    public Traversal.Admin<S, ?> getEmitTraversal() {
        return this.emitTraversal;
    }

    public Traversal.Admin<S, S> getRepeatTraversal() {
        return this.repeatTraversal;
    }

    public List<Traversal.Admin<S, S>> getGlobalChildren() {
        return null == this.repeatTraversal ? Collections.emptyList() : Collections.singletonList(this.repeatTraversal);
    }

    public List<Traversal.Admin<S, ?>> getLocalChildren() {
        final List<Traversal.Admin<S, ?>> list = new ArrayList<>(2);
        if (null != this.untilTraversal)
            list.add(this.untilTraversal);
        if (null != this.emitTraversal)
            list.add(this.emitTraversal);
        return list;
    }

    public final boolean doUntil(final Traverser.Admin<S> traverser, boolean utilFirst) {
        return utilFirst == this.untilFirst && null != this.untilTraversal && TraversalUtil.test(traverser, this.untilTraversal);
    }

    public final boolean doEmit(final Traverser.Admin<S> traverser, boolean emitFirst) {
        return emitFirst == this.emitFirst && null != this.emitTraversal && TraversalUtil.test(traverser, this.emitTraversal);
    }

    @Override
    public String toString() {
        if (this.untilFirst && this.emitFirst)
            return StringFactory.stepString(this, untilString(), emitString(), this.repeatTraversal);
        else if (this.emitFirst)
            return StringFactory.stepString(this, emitString(), this.repeatTraversal, untilString());
        else if (this.untilFirst)
            return StringFactory.stepString(this, untilString(), this.repeatTraversal, emitString());
        else
            return StringFactory.stepString(this, this.repeatTraversal, untilString(), emitString());
    }

    @Override
    public void reset() {
        super.reset();
        if (null != this.emitTraversal)
            this.emitTraversal.reset();
        if (null != this.untilTraversal)
            this.untilTraversal.reset();
        if (null != this.repeatTraversal)
            this.repeatTraversal.reset();
    }

    private final String untilString() {
        return null == this.untilTraversal ? "until(false)" : "until(" + this.untilTraversal + ')';
    }

    private final String emitString() {
        return null == this.emitTraversal ? "emit(false)" : "emit(" + this.emitTraversal + ')';
    }

    /////////////////////////

    @Override
    public RepeatStep<S> clone() {
        final RepeatStep<S> clone = (RepeatStep<S>) super.clone();
        clone.repeatTraversal = this.repeatTraversal.clone();
        if (null != this.untilTraversal)
            clone.untilTraversal = this.untilTraversal.clone();
        if (null != this.emitTraversal)
            clone.emitTraversal = this.emitTraversal.clone();
        return clone;
    }

    @Override
    public void setTraversal(final Traversal.Admin<?, ?> parentTraversal) {
        super.setTraversal(parentTraversal);
        this.integrateChild(this.repeatTraversal);
        this.integrateChild(this.untilTraversal);
        this.integrateChild(this.emitTraversal);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode() ^ this.repeatTraversal.hashCode();
        result ^= Boolean.hashCode(this.untilFirst);
        result ^= Boolean.hashCode(this.emitFirst) << 1;
        if (this.loopName != null)
            result ^= this.loopName.hashCode();
        if (this.untilTraversal != null)
            result ^= this.untilTraversal.hashCode();
        if (this.emitTraversal != null)
            result ^= this.emitTraversal.hashCode();
        return result;
    }

    private final LinkedList<Traverser.Admin<S>> stashedStarts = new LinkedList<>();

    private Traverser.Admin<S> nextStart(final boolean useDfs) {
		if (!useDfs) {
			return this.starts.next();
		} else {
			if (this.starts.hasNext()) {
				return this.starts.next();
			} else {
				return this.stashedStarts.pop();
			}
		}
    }

    @Override
    protected Iterator<Traverser.Admin<S>> standardAlgorithm() throws NoSuchElementException {
        if (null == this.repeatTraversal)
            throw new IllegalStateException("The repeat()-traversal was not defined: " + this);
        final List<Step> steps = this.repeatTraversal.getSteps();
        final Step stepBeforeRepeatEndStep = steps.get(steps.size() - 2);
        final boolean useDfs = !(stepBeforeRepeatEndStep instanceof Barrier);
        while (true) {
            if (this.repeatTraversal.getEndStep().hasNext()) {
                return this.repeatTraversal.getEndStep();
            } else {
                final Traverser.Admin<S> start = nextStart(useDfs);
                start.initialiseLoops(this.getId(), this.loopName);
                if (useDfs) {
                    final List<Traverser.Admin<S>> localStarts = new ArrayList<>();
                    while (this.starts.hasNext()) {
                        localStarts.add(this.starts.next());
                    }
                    stashedStarts.addAll(0, localStarts);
                }
                if (doUntil(start, true)) {
                    start.resetLoops();
                    return IteratorUtils.of(start);
                }
                this.repeatTraversal.addStart(start);
                if (doEmit(start, true)) {
                    final Traverser.Admin<S> emitSplit = start.split();
                    emitSplit.resetLoops();
                    return IteratorUtils.of(emitSplit);
                }
            }
        }
    }

    @Override
    protected Iterator<Traverser.Admin<S>> computerAlgorithm() throws NoSuchElementException {
        if (null == this.repeatTraversal)
            throw new IllegalStateException("The repeat()-traversal was not defined: " + this);

        final Traverser.Admin<S> start = this.starts.next();
        if (doUntil(start, true)) {
            start.setStepId(this.getNextStep().getId());
            start.addLabels(this.labels);
            return IteratorUtils.of(start);
        } else {
            start.setStepId(this.repeatTraversal.getStartStep().getId());
            start.initialiseLoops(start.getStepId(), this.loopName);
            if (doEmit(start, true)) {
                final Traverser.Admin<S> emitSplit = start.split();
                emitSplit.resetLoops();
                emitSplit.setStepId(this.getNextStep().getId());
                return IteratorUtils.of(start, emitSplit);
            } else {
                return IteratorUtils.of(start);
            }
        }
    }

    /////////////////////////

    public static <A, B, C extends Traversal<A, B>> C addRepeatToTraversal(final C traversal, final Traversal.Admin<B, B> repeatTraversal) {
        final Step<?, B> step = traversal.asAdmin().getEndStep();
        boolean setBfs = false;
        if (step instanceof RepeatStep && null == ((RepeatStep) step).repeatTraversal) {
            ((RepeatStep<B>) step).setRepeatTraversal(repeatTraversal);
        } else {
            final RepeatStep<B> repeatStep = new RepeatStep<>(traversal.asAdmin());
            List<Step> steps = repeatTraversal.getSteps();
            repeatStep.setRepeatTraversal(repeatTraversal);
            traversal.asAdmin().addStep(repeatStep);
        }
        return traversal;
    }

    public static <A, B, C extends Traversal<A, B>> C addRepeatToTraversal(final C traversal, final String loopName, final Traversal.Admin<B, B> repeatTraversal) {
        addRepeatToTraversal(traversal, repeatTraversal);
        final Step<?, B> step = traversal.asAdmin().getEndStep();
        ((RepeatStep) step).loopName = loopName;
        return traversal;
    }

    public static <A, B, C extends Traversal<A, B>> C addUntilToTraversal(final C traversal, final Traversal.Admin<B, ?> untilPredicate) {
        final Step<?, B> step = traversal.asAdmin().getEndStep();
        if (step instanceof RepeatStep && null == ((RepeatStep) step).untilTraversal) {
            ((RepeatStep<B>) step).setUntilTraversal(untilPredicate);
        } else {
            final RepeatStep<B> repeatStep = new RepeatStep<>(traversal.asAdmin());
            repeatStep.setUntilTraversal(untilPredicate);
            traversal.asAdmin().addStep(repeatStep);
        }
        return traversal;
    }

    public static <A, B, C extends Traversal<A, B>> C addEmitToTraversal(final C traversal, final Traversal.Admin<B, ?> emitPredicate) {
        final Step<?, B> step = traversal.asAdmin().getEndStep();
        if (step instanceof RepeatStep && null == ((RepeatStep) step).emitTraversal) {
            ((RepeatStep<B>) step).setEmitTraversal(emitPredicate);
        } else {
            final RepeatStep<B> repeatStep = new RepeatStep<>(traversal.asAdmin());
            repeatStep.setEmitTraversal(emitPredicate);
            traversal.asAdmin().addStep(repeatStep);
        }
        return traversal;
    }

    ///////////////////////////////////

    public static class RepeatEndStep<S> extends ComputerAwareStep<S, S> {

        public RepeatEndStep(final Traversal.Admin traversal) {
            super(traversal);
        }

        final LinkedList<Traverser.Admin<S>> stashedStarts = new LinkedList<>();

        private Traverser.Admin<S> nextStart(RepeatStep<S> repeatStep, boolean useDfs) {
            if (!useDfs) {
                return this.starts.next();
            } else {
                if (this.starts.hasNext()) {
                    return this.starts.next();
                } else {
                    return this.stashedStarts.pop();
                }
            }
        }

        @Override
        public boolean hasNext() {
            return super.hasNext() || !this.stashedStarts.isEmpty();
        }

        @Override
        protected Iterator<Traverser.Admin<S>> standardAlgorithm() throws NoSuchElementException {
            final RepeatStep<S> repeatStep = (RepeatStep<S>) this.getTraversal().getParent();
            final List<Step> steps = repeatStep.repeatTraversal.getSteps();
            final Step stepBeforeRepeatEndStep = steps.get(steps.size() - 2);
            final boolean useDfs = !(stepBeforeRepeatEndStep instanceof Barrier);
            while (true) {
                final Traverser.Admin<S> start = nextStart(repeatStep, useDfs);
                start.incrLoops();
                if (useDfs) {
                    final List<Traverser.Admin<S>> localStarts = new ArrayList<>();
                    while (this.starts.hasNext()) {
                        localStarts.add(this.starts.next());
                    }
                    stashedStarts.addAll(0, localStarts);
                }
                start.incrLoops();
                if (repeatStep.doUntil(start, false)) {
                    start.resetLoops();
                    return IteratorUtils.of(start);
                } else {
                    if (!repeatStep.untilFirst && !repeatStep.emitFirst)
                        repeatStep.repeatTraversal.addStart(start);
                    else
                        repeatStep.addStart(start);
                    if (repeatStep.doEmit(start, false)) {
                        final Traverser.Admin<S> emitSplit = start.split();
                        emitSplit.resetLoops();
                        return IteratorUtils.of(emitSplit);
                    }
                }
            }
        }

        @Override
        protected Iterator<Traverser.Admin<S>> computerAlgorithm() throws NoSuchElementException {
            final RepeatStep<S> repeatStep = (RepeatStep<S>) this.getTraversal().getParent();
            final Traverser.Admin<S> start = this.starts.next();
            start.incrLoops();
            if (repeatStep.doUntil(start, false)) {
                start.resetLoops();
                start.setStepId(repeatStep.getNextStep().getId());
                start.addLabels(repeatStep.labels);
                return IteratorUtils.of(start);
            } else {
                start.setStepId(repeatStep.getId());
                if (repeatStep.doEmit(start, false)) {
                    final Traverser.Admin<S> emitSplit = start.split();
                    emitSplit.resetLoops();
                    emitSplit.setStepId(repeatStep.getNextStep().getId());
                    emitSplit.addLabels(repeatStep.labels);
                    return IteratorUtils.of(start, emitSplit);
                }
                return IteratorUtils.of(start);
            }
        }
    }

}
