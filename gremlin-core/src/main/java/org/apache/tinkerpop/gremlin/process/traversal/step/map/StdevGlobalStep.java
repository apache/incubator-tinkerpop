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
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ReducingBarrierStep;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.util.NumberHelper;
import org.apache.tinkerpop.gremlin.util.function.StdevNumberSupplier;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import static org.apache.tinkerpop.gremlin.util.NumberHelper.div;
import static org.apache.tinkerpop.gremlin.util.NumberHelper.mul;
import static org.apache.tinkerpop.gremlin.util.NumberHelper.sub;

/**
 * @author Junshi Guo
 */
public class StdevGlobalStep<S extends Number, E extends Number> extends ReducingBarrierStep<S, E> {

    private static final Set<TraverserRequirement> REQUIREMENTS = EnumSet.of(TraverserRequirement.OBJECT, TraverserRequirement.BULK);

    public StdevGlobalStep(final Traversal.Admin traversal) {
        super(traversal);
        this.setSeedSupplier((Supplier) StdevNumberSupplier.instance());
        this.setReducingBiOperator(StdevGlobalBiOperator.INSTANCE);
    }

    @Override
    public void processAllStarts() {
        if (this.starts.hasNext()) {
            super.processAllStarts();
        }
    }

    @Override
    public E projectTraverser(final Traverser.Admin<S> traverser) {
        return (E) new StdevNumber(traverser.get(), traverser.bulk());
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    public E generateFinalResult(final E stdevNumber) {
        return (E) ((StdevNumber) stdevNumber).getFinal();
    }

    ////////

    private static final class StdevGlobalBiOperator<S extends Number> implements BinaryOperator<S>, Serializable {

        private static final StdevGlobalBiOperator INSTANCE = new StdevGlobalBiOperator<>();

        @Override
        public S apply(final S mutatingSeed, final S number) {
            if (mutatingSeed instanceof StdevNumber) {
                return number instanceof StdevNumber ?
                        (S) ((StdevNumber) mutatingSeed).add((StdevNumber) number) :
                        (S) ((StdevNumber) mutatingSeed).add(number, 1l);
            } else {
                return number instanceof StdevNumber ?
                        (S) ((StdevNumber) number).add(mutatingSeed, 1l) :
                        (S) new StdevNumber(number, 1l).add(mutatingSeed, 1l);
            }
        }
    }

    public static final class StdevNumber extends Number implements Comparable<Number> {

        private Number sum;
        private Number squareSum;
        private long count;

        public StdevNumber() {
            this(0, 0);
        }

        public StdevNumber(final Number value, final long count) {
            this.count = count;
            this.sum = mul(value, count);
            this.squareSum = mul(mul(value, value), count);
        }

        public StdevNumber add(final Number value, final long count) {
            this.count += count;
            this.sum = NumberHelper.add(sum, mul(value, count));
            this.squareSum = NumberHelper.add(squareSum, mul(mul(value, value), count));
            return this;
        }

        public StdevNumber add(final StdevNumber other) {
            this.count += other.count;
            this.sum = NumberHelper.add(sum, other.sum);
            this.squareSum = NumberHelper.add(squareSum, other.squareSum);
            return this;
        }

        @Override
        public int intValue() {
            return getFinal().intValue();
        }

        @Override
        public long longValue() {
            return getFinal().longValue();
        }

        @Override
        public float floatValue() {
            return getFinal().floatValue();
        }

        @Override
        public double doubleValue() {
            return getFinal().doubleValue();
        }

        @Override
        public String toString() {
            return getFinal().toString();
        }

        @Override
        public int compareTo(final Number number) {
            return Double.compare(this.doubleValue(), number.doubleValue());
        }

        @Override
        public boolean equals(final Object object) {
            return object instanceof Number && Double.valueOf(this.doubleValue()).equals(((Number) object).doubleValue());
        }

        @Override
        public int hashCode() {
            return Double.valueOf(this.doubleValue()).hashCode();
        }

        public Number getFinal() {
            Number mean = div(sum, count, true);
            Number variation = sub(div(squareSum, count, true), mul(mean, mean));
            return Math.sqrt(variation.doubleValue());
        }
    }
}
