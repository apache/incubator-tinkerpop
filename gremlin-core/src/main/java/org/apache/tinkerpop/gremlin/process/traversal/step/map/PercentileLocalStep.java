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
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.apache.tinkerpop.gremlin.util.NumberHelper.percentile;
import static org.apache.tinkerpop.gremlin.util.NumberHelper.percentiles;

/**
 * @author Junshi Guo
 */
public class PercentileLocalStep<E extends Number, S extends Iterable<E>> extends ScalarMapStep<S, Object> {

    private final int[] percentiles;

    public PercentileLocalStep(final Traversal.Admin traversal, final int... percentiles) {
        super(traversal);
        this.percentiles = percentiles;
        if (percentiles == null || percentiles.length == 0) {
            throw new IllegalArgumentException("Percentile step should be provided at least one argument.");
        }
    }

    @Override
    protected Object map(final Traverser.Admin<S> traverser) {
        final Iterator<E> iterator = traverser.get().iterator();
        if (!iterator.hasNext()) {
            throw FastNoSuchElementException.instance();
        }
        List<E> buffer = new ArrayList<>();
        iterator.forEachRemaining(result -> buffer.add(result));
        if (percentiles.length == 1) {
            return percentile(buffer, percentiles[0]);
        } else {
            return percentiles(buffer, percentiles);
        }
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return Collections.singleton(TraverserRequirement.OBJECT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PercentileLocalStep<?, ?> that = (PercentileLocalStep<?, ?>) o;
        return Arrays.equals(percentiles, that.percentiles);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(percentiles);
        return result;
    }
}
