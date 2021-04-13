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

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser.Admin;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import static org.apache.tinkerpop.gremlin.util.NumberHelper.div;
import static org.apache.tinkerpop.gremlin.util.NumberHelper.mul;
import static org.apache.tinkerpop.gremlin.util.NumberHelper.add;
import static org.apache.tinkerpop.gremlin.util.NumberHelper.sub;

/**
 * @author Junshi Guo
 */
public class StdevLocalStep<E extends Number, S extends Iterable<E>> extends ScalarMapStep<S, Number> {

    public StdevLocalStep(Traversal.Admin traversal) {
        super(traversal);
    }

    @Override
    protected Number map(final Admin<S> traverser) {
        final Iterator<E> iterator = traverser.get().iterator();
        if (!iterator.hasNext()) {
            throw FastNoSuchElementException.instance();
        }

        Number sum = null;
        Number squareSum = null;
        int count = 0;
        while (iterator.hasNext()) {
            E value = iterator.next();
            if (sum == null) {
                sum = value;
                squareSum = mul(value, value);
            } else {
                sum = add(sum, value);
                squareSum = add(squareSum, mul(value, value));
            }
            count++;
        }
        Number mean = div(sum, count, true);
        Number variation = sub(div(squareSum, count, true), mul(mean, mean));
        return Math.sqrt(variation.doubleValue());
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return Collections.singleton(TraverserRequirement.OBJECT);
    }
}
