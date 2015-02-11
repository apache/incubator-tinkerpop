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
package org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure;

import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.computer.Memory;
import org.apache.tinkerpop.gremlin.process.computer.MessageScope;
import org.apache.tinkerpop.gremlin.process.computer.Messenger;
import org.apache.tinkerpop.gremlin.process.computer.util.AbstractVertexProgramBuilder;
import org.apache.tinkerpop.gremlin.process.computer.util.LambdaHolder;
import org.apache.tinkerpop.gremlin.process.computer.util.StaticVertexProgram;
import org.apache.tinkerpop.gremlin.process.computer.util.VertexProgramHelper;
import org.apache.tinkerpop.gremlin.process.graph.traversal.__;
import org.apache.tinkerpop.gremlin.process.util.MapHelper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.StreamFactory;
import org.apache.commons.configuration.Configuration;
import org.javatuples.Pair;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PeerPressureVertexProgram extends StaticVertexProgram<Pair<Serializable, Double>> {

    private MessageScope.Local<?> voteScope = MessageScope.Local.of(__::outE);
    private MessageScope.Local<?> countScope = MessageScope.Local.of(new MessageScope.Local.ReverseTraversalSupplier(this.voteScope));
    private final Set<MessageScope> VOTE_SCOPE = new HashSet<>(Collections.singletonList(this.voteScope));
    private final Set<MessageScope> COUNT_SCOPE = new HashSet<>(Collections.singletonList(this.countScope));

    public static final String CLUSTER = "gremlin.peerPressureVertexProgram.cluster";
    public static final String VOTE_STRENGTH = "gremlin.peerPressureVertexProgram.voteStrength";

    private static final String MAX_ITERATIONS = "gremlin.peerPressureVertexProgram.maxIterations";
    private static final String DISTRIBUTE_VOTE = "gremlin.peerPressureVertexProgram.distributeVote";
    private static final String INCIDENT_TRAVERSAL_SUPPLIER = "gremlin.peerPressureVertexProgram.incidentTraversalSupplier";
    private static final String VOTE_TO_HALT = "gremlin.peerPressureVertexProgram.voteToHalt";

    private LambdaHolder<Supplier<Traversal<Vertex, Edge>>> traversalSupplier;
    private int maxIterations = 30;
    private boolean distributeVote = false;

    private static final Set<String> ELEMENT_COMPUTE_KEYS = new HashSet<>(Arrays.asList(CLUSTER, VOTE_STRENGTH));
    private static final Set<String> MEMORY_COMPUTE_KEYS = new HashSet<>(Collections.singletonList(VOTE_TO_HALT));

    private PeerPressureVertexProgram() {

    }

    @Override
    public void loadState(final Configuration configuration) {
        this.traversalSupplier = LambdaHolder.loadState(configuration, INCIDENT_TRAVERSAL_SUPPLIER);
        if (null != this.traversalSupplier) {
            VertexProgramHelper.verifyReversibility(this.traversalSupplier.get().get().asAdmin());
            this.voteScope = MessageScope.Local.of(this.traversalSupplier.get());
            this.countScope = MessageScope.Local.of(new MessageScope.Local.ReverseTraversalSupplier(this.voteScope));
        }
        this.maxIterations = configuration.getInt(MAX_ITERATIONS, 30);
        this.distributeVote = configuration.getBoolean(DISTRIBUTE_VOTE, false);
    }

    @Override
    public void storeState(final Configuration configuration) {
        super.storeState(configuration);
        this.traversalSupplier.storeState(configuration);
        configuration.setProperty(MAX_ITERATIONS, this.maxIterations);
        configuration.setProperty(DISTRIBUTE_VOTE, this.distributeVote);

    }

    @Override
    public Set<String> getElementComputeKeys() {
        return ELEMENT_COMPUTE_KEYS;
    }

    @Override
    public Set<String> getMemoryComputeKeys() {
        return MEMORY_COMPUTE_KEYS;
    }

    @Override
    public Set<MessageScope> getMessageScopes(final Memory memory) {
        return this.distributeVote && memory.isInitialIteration() ? COUNT_SCOPE : VOTE_SCOPE;
    }

    @Override
    public void setup(final Memory memory) {
        memory.set(VOTE_TO_HALT, false);
    }

    @Override
    public void execute(final Vertex vertex, Messenger<Pair<Serializable, Double>> messenger, final Memory memory) {
        if (memory.isInitialIteration()) {
            if (this.distributeVote) {
                messenger.sendMessage(this.countScope, Pair.with('c', 1.0d));
            } else {
                double voteStrength = 1.0d;
                vertex.property(VertexProperty.Cardinality.single, CLUSTER, vertex.id());
                vertex.property(VertexProperty.Cardinality.single, VOTE_STRENGTH, voteStrength);
                messenger.sendMessage(this.voteScope, new Pair<>((Serializable) vertex.id(), voteStrength));
                memory.and(VOTE_TO_HALT, false);
            }
        } else if (1 == memory.getIteration() && this.distributeVote) {
            double voteStrength = 1.0d / StreamFactory.stream(messenger.receiveMessages(this.countScope)).map(Pair::getValue1).reduce(0.0d, (a, b) -> a + b);
            vertex.property(VertexProperty.Cardinality.single, CLUSTER, vertex.id());
            vertex.property(VertexProperty.Cardinality.single, VOTE_STRENGTH, voteStrength);
            messenger.sendMessage(this.voteScope, new Pair<>((Serializable) vertex.id(), voteStrength));
            memory.and(VOTE_TO_HALT, false);
        } else {
            final Map<Serializable, Double> votes = new HashMap<>();
            votes.put(vertex.value(CLUSTER), vertex.<Double>value(VOTE_STRENGTH));
            messenger.receiveMessages(this.voteScope).forEach(message -> MapHelper.incr(votes, message.getValue0(), message.getValue1()));
            Serializable cluster = PeerPressureVertexProgram.largestCount(votes);
            if (null == cluster) cluster = (Serializable) vertex.id();
            memory.and(VOTE_TO_HALT, vertex.value(CLUSTER).equals(cluster));
            vertex.property(VertexProperty.Cardinality.single, CLUSTER, cluster);
            messenger.sendMessage(this.voteScope, new Pair<>(cluster, vertex.<Double>value(VOTE_STRENGTH)));
        }
    }

    @Override
    public boolean terminate(final Memory memory) {
        final boolean voteToHalt = memory.<Boolean>get(VOTE_TO_HALT) || memory.getIteration() >= (this.distributeVote ? this.maxIterations + 1 : this.maxIterations);
        if (voteToHalt) {
            return true;
        } else {
            memory.or(VOTE_TO_HALT, true);
            return false;
        }
    }

    private static <T> T largestCount(final Map<T, Double> map) {
        T largestKey = null;
        double largestValue = Double.MIN_VALUE;
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            if (entry.getValue() == largestValue) {
                if (null != largestKey && largestKey.toString().compareTo(entry.getKey().toString()) > 0) {
                    largestKey = entry.getKey();
                    largestValue = entry.getValue();
                }
            } else if (entry.getValue() > largestValue) {
                largestKey = entry.getKey();
                largestValue = entry.getValue();
            }
        }
        return largestKey;
    }

    @Override
    public String toString() {
        return StringFactory.vertexProgramString(this, "distributeVote=" + this.distributeVote + ",maxIterations=" + this.maxIterations);
    }

    //////////////////////////////

    public static Builder build() {
        return new Builder();
    }

    public static class Builder extends AbstractVertexProgramBuilder<Builder> {


        private Builder() {
            super(PeerPressureVertexProgram.class);
        }

        public Builder maxIterations(final int iterations) {
            this.configuration.setProperty(MAX_ITERATIONS, iterations);
            return this;
        }

        public Builder distributeVote(final boolean distributeVote) {
            this.configuration.setProperty(DISTRIBUTE_VOTE, distributeVote);
            return this;
        }

        public Builder incident(final String scriptEngine, final String traversalScript) {
            LambdaHolder.storeState(this.configuration, LambdaHolder.Type.SCRIPT, INCIDENT_TRAVERSAL_SUPPLIER, new String[]{scriptEngine, traversalScript});
            return this;
        }

        public Builder incident(final String traversalScript) {
            return incident(GREMLIN_GROOVY, traversalScript);
        }

        public Builder incident(final Supplier<Traversal<Vertex, Edge>> traversal) {
            LambdaHolder.storeState(this.configuration, LambdaHolder.Type.OBJECT, INCIDENT_TRAVERSAL_SUPPLIER, traversal);
            return this;
        }

        public Builder incident(final Class<Supplier<Traversal<Vertex, Edge>>> traversalClass) {
            LambdaHolder.storeState(this.configuration, LambdaHolder.Type.CLASS, INCIDENT_TRAVERSAL_SUPPLIER, traversalClass);
            return this;
        }

    }

    ////////////////////////////

    @Override
    public Features getFeatures() {
        return new Features() {
            @Override
            public boolean requiresLocalMessageScopes() {
                return true;
            }

            @Override
            public boolean requiresVertexPropertyAddition() {
                return true;
            }
        };
    }
}
