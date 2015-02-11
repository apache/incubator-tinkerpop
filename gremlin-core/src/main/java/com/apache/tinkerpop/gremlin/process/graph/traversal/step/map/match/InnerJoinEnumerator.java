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
package com.apache.tinkerpop.gremlin.process.graph.traversal.step.map.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * An Enumerator which joins the solutions of a base Enumerator according to repeated variables
 * <p/>
 * Note: this Enumerator requires random access to its base Enumerator,
 * as it maintains a list of indices at which valid solutions are found, and visits only those indices.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class InnerJoinEnumerator<T> implements Enumerator<T> {
    private final Enumerator<T> baseEnumerator;
    private Iterator<Integer> iterator;
    private final List<Integer> joinIndices;

    private final Map<String, T> map;
    private final BiConsumer<String, T> joinVisitor;

    private int joinCount;

    public InnerJoinEnumerator(final Enumerator<T> baseEnumerator,
                               final Set<String> joinVariables) {

        this.baseEnumerator = baseEnumerator;
        this.joinIndices = new ArrayList<>();

        map = new HashMap<>();
        // TODO: allow for more than two instances of a variable
        joinVisitor = (name, newValue) -> {
            if (joinVariables.contains(name)) {
                T value = map.get(name);
                if (null == value) {
                    map.put(name, newValue);
                } else if (value.equals(newValue)) {
                    joinCount++;
                }
            } else {
                map.put(name, newValue);
            }
        };

        iterator = new Iterator<Integer>() {
            private int currentIndex = -1;

            {
                advanceToNext();
            }

            public boolean hasNext() {
                // there are no calls to this method
                return false;
            }

            public Integer next() {
                int tmp = currentIndex;
                advanceToNext();
                return tmp;
            }

            private void advanceToNext() {
                while (true) {
                    map.clear();
                    joinCount = 0;

                    if (!baseEnumerator.visitSolution(++currentIndex, joinVisitor)) {
                        iterator = null;
                        return;
                    }

                    if (joinVariables.size() == joinCount) {
                        joinIndices.add(currentIndex);
                        return;
                    }
                }
            }
        };
    }

    public int size() {
        return joinIndices.size();
    }

    public boolean visitSolution(int i, BiConsumer<String, T> visitor) {
        while (i >= joinIndices.size()) {
            if (null == iterator) {
                return false;
            }

            iterator.next();
        }

        map.clear();
        if (!baseEnumerator.visitSolution(joinIndices.get(i), joinVisitor)) {
            throw new IllegalStateException();
        }

        for (Map.Entry<String, T> entry : map.entrySet()) {
            visitor.accept(entry.getKey(), entry.getValue());
        }

        return true;
    }
}
