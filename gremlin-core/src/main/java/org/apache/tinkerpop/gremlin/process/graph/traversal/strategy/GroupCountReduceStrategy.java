/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tinkerpop.gremlin.process.graph.traversal.strategy;

import org.apache.tinkerpop.gremlin.process.Step;
import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.graph.traversal.step.branch.LocalStep;
import org.apache.tinkerpop.gremlin.process.graph.traversal.step.map.GroupCountReduceStep;
import org.apache.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.GroupCountSideEffectStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public final class GroupCountReduceStrategy extends AbstractTraversalStrategy implements TraversalStrategy {

    private static final GroupCountReduceStrategy INSTANCE = new GroupCountReduceStrategy();

    private GroupCountReduceStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        final Step endStep = traversal.getEndStep();
        if (endStep instanceof GroupCountSideEffectStep && traversal.getParent() instanceof LocalStep) {
            final GroupCountReduceStep reduceStep = new GroupCountReduceStep(traversal);
            for (final Object child : ((GroupCountSideEffectStep) endStep).getLocalChildren()) {
                reduceStep.addLocalChild((Traversal.Admin) child);
            }
            TraversalHelper.replaceStep(endStep, reduceStep, traversal);
        }
    }

    public static GroupCountReduceStrategy instance() {
        return INSTANCE;
    }
}
