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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Junshi Guo
 */
public class PercentileGlobalStep<S extends Number> extends ReducingBarrierStep<S, Object> {

    private static final Set<TraverserRequirement> REQUIREMENTS = EnumSet.of(TraverserRequirement.OBJECT, TraverserRequirement.BULK);
    List<S> buffer;
    private final int[] percentiles;

    public PercentileGlobalStep(final Traversal.Admin traversal, final int... percentiles) {
        super(traversal);
        this.buffer = new ArrayList<>();
        this.percentiles = percentiles;
        this.setSeedSupplier(() -> Collections.emptyMap());
        this.setReducingBiOperator((x, y) -> Collections.emptyMap());
    }

    @Override
    public void reset() {
        super.reset();
        this.buffer.clear();
    }

    @Override
    public void done() {
        super.done();
        this.buffer.clear();
    }

    @Override
    public void processAllStarts() {
        if (this.starts.hasNext()) {
            super.processAllStarts();
        }
    }

    @Override
    public Object projectTraverser(final Traverser.Admin<S> traverser) {
        Stream.iterate(0, n -> n + 1).limit(traverser.bulk()).forEach(n -> this.buffer.add(traverser.get()));
        return Collections.emptyMap();
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    public Object generateFinalResult(final Object percentileNumber) {
        if (this.percentiles.length > 1) {
            return NumberHelper.percentiles(this.buffer, percentiles);
        } else if (this.percentiles.length == 1) {
            return NumberHelper.percentile(this.buffer, percentiles[0]);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PercentileGlobalStep<?> that = (PercentileGlobalStep<?>) o;
        return Arrays.equals(percentiles, that.percentiles);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(percentiles);
        return result;
    }
}
