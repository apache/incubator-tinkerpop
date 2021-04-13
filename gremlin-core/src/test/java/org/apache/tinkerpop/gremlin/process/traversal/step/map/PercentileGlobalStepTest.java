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

import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.StepTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Junshi Guo
 */
public class PercentileGlobalStepTest extends StepTest {
    @Override
    protected List<Traversal> getTraversals() {
        return Arrays.asList(
                __.percentile(50),
                __.percentile(50, 100),
                __.percentile(Scope.global, 30),
                __.percentile(Scope.global, 30, 60)
        );
    }

    @Test
    public void testReturnTypes() {
        final List<Integer> list = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            list.add(i);
        }
        assertEquals(50, __.inject(list).unfold().percentile(Scope.global, 50).next());
        Object multiPercentile = __.inject(list).unfold().percentile(50, 100).next();
        assertTrue(multiPercentile instanceof Map);
        Map mapResult = (Map) multiPercentile;
        assertEquals(2, mapResult.size());
        assertEquals(50, mapResult.get(50));
        assertEquals(100, mapResult.get(100));
    }
}
