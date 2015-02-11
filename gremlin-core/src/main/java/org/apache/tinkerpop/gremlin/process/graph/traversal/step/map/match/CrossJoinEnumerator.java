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
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.map.match;

import java.util.function.BiConsumer;

/**
 * An Enumerator which finds the Cartesian product of two other Enumerators,
 * expanding at the same rate in either dimension.
 * This maximizes the size of the product with respect to the number of expansions of the base Enumerators.
 */
public class CrossJoinEnumerator<T> implements Enumerator<T> {

    private final Enumerator<T> xEnum, yEnum;

    public CrossJoinEnumerator(final Enumerator<T> xEnum,
                               final Enumerator<T> yEnum) {
        this.xEnum = xEnum;
        this.yEnum = yEnum;
    }

    public int size() {
        return xEnum.size() * yEnum.size();
    }

    // note: permits random access
    public boolean visitSolution(final int index,
                                 final BiConsumer<String, T> visitor) {
        int sq = (int) Math.sqrt(index);

        // choose x and y such that the solution represented by index
        // remains constant as this Enumerator expands
        int x, y;

        if (0 == index) {
            x = y = 0;
        } else {
            int r = index - sq * sq;
            if (r < sq) {
                x = sq;
                y = r;
            } else {
                x = r - sq;
                y = sq;
            }
        }

        // expand x if necessary
        if (!hasIndex(xEnum, sq)) {
            if (0 == xEnum.size()) {
                return false;
            }

            x = index % xEnum.size();
            y = index / xEnum.size();
        }

        // expand y if necessary
        if (!hasIndex(yEnum, y)) {
            int height = yEnum.size();
            if (0 == height) {
                return false;
            }

            x = index / height;
            if (!hasIndex(xEnum, x)) {
                return false;
            }
            y = index % height;
        }

        // solutions are visited completely (if we have reached this point), else not at all
        return xEnum.visitSolution(x, visitor) && yEnum.visitSolution(y, visitor);
    }

    private boolean hasIndex(final Enumerator<T> e,
                             final int index) {
        return index < e.size() || e.visitSolution(index, (BiConsumer<String, T>) MatchStep.TRIVIAL_CONSUMER);
    }
}
