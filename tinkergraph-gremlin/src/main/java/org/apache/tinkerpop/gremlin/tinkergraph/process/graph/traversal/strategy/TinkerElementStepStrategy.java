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
package com.tinkerpop.gremlin.tinkergraph.process.graph.traversal.strategy;

import com.tinkerpop.gremlin.process.Step;
import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.TraversalEngine;
import com.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.GraphStep;
import com.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.IdentityStep;
import com.tinkerpop.gremlin.process.graph.traversal.step.sideEffect.StartStep;
import com.tinkerpop.gremlin.process.graph.traversal.strategy.AbstractTraversalStrategy;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerElementStepStrategy extends AbstractTraversalStrategy {

    private static final TinkerElementStepStrategy INSTANCE = new TinkerElementStepStrategy();

    private TinkerElementStepStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal, final TraversalEngine engine) {
        if (engine.equals(TraversalEngine.STANDARD))
            return;

        final StartStep<Element> startStep = (StartStep<Element>) traversal.getStartStep();
        if (startStep.startAssignableTo(Vertex.class, Edge.class)) {
            final Element element = ((StartStep<?>) startStep).getStart();
            traversal.removeStep(startStep);
            startStep.getLabel().ifPresent(label -> {
                final Step identityStep = new IdentityStep(traversal);
                identityStep.setLabel(label);
                traversal.addStep(0, identityStep);
            });
            traversal.addStep(0, new GraphStep<>(traversal, EmptyGraph.instance(), element.getClass(), element.id()));
        }
    }

    public static TinkerElementStepStrategy instance() {
        return INSTANCE;
    }

}
