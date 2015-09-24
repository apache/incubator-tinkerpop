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

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.StepTest;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public class OrderGlobalStepTest extends StepTest {

    @Override
    @Ignore("hashCode() doesn't work properly for .order().by()")
    public void testEquality() {
    }

    @Override
    protected List<Traversal> getTraversals() {
        return Arrays.asList(
                __.order(),
                __.order().by(Order.decr),
                __.order().by("age", Order.incr),
                __.order().by("age", Order.decr),
                __.order().by(outE().count(), Order.incr),
                __.order().by("age", Order.incr).by(outE().count(), Order.incr),
                __.order().by(outE().count(), Order.incr).by("age", Order.incr)
        );
    }
}
