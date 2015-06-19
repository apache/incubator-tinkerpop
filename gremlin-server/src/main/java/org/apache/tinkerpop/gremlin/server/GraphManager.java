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
package org.apache.tinkerpop.gremlin.server;

import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder for {@link Graph} and {@link TraversalSource} instances configured for the server to be passed to script
 * engine bindings. The {@link Graph} instances are read from the {@link Settings} for Gremlin Server as defined in
 * the configuration file. The {@link TraversalSource} instances are rebound to the {@code GraphManager} once
 * initialization scripts construct them.
 */
public final class GraphManager {
    private static final Logger logger = LoggerFactory.getLogger(GremlinServer.class);

    private final Map<String, Graph> graphs = new ConcurrentHashMap<>();
    private final Map<String, TraversalSource> traversalSources = new ConcurrentHashMap<>();

    /**
     * Create a new instance using the {@link Settings} from Gremlin Server.
     */
    public GraphManager(final Settings settings) {
        settings.graphs.entrySet().forEach(e -> {
            try {
                final Graph newGraph = GraphFactory.open(e.getValue());
                graphs.put(e.getKey(), newGraph);
                logger.info("Graph [{}] was successfully configured via [{}].", e.getKey(), e.getValue());
            } catch (RuntimeException re) {
                logger.warn(String.format("Graph [%s] configured at [%s] could not be instantiated and will not be available in Gremlin Server.  GraphFactory message: %s",
                        e.getKey(), e.getValue(), re.getMessage()), re);
                if (re.getCause() != null) logger.debug("GraphFactory exception", re.getCause());
            }
        });
    }

    /**
     * Get a list of the {@link Graph} instances and their binding names as defined in the Gremlin Server
     * configuration file.
     *
     * @return a {@link Map} where the key is the name of the {@link Graph} and the value is the {@link Graph} itself
     */
    public Map<String, Graph> getGraphs() {
        return graphs;
    }

    /**
     * Get a list of the {@link TraversalSource} instances and their binding names as defined by Gremlin Server
     * initialization scripts.
     *
     * @return a {@link Map} where the key is the name of the {@link TraversalSource} and the value is the
     *         {@link TraversalSource} itself
     */
    public Map<String, TraversalSource> getTraversalSources() {
        return traversalSources;
    }

    /**
     * Get the {@link Graph} and {@link TraversalSource} list as a set of bindings.
     */
    public Bindings getAsBindings() {
        final Bindings bindings = new SimpleBindings();
        graphs.forEach(bindings::put);
        traversalSources.forEach(bindings::put);
        return bindings;
    }

    /**
     * Rollback transactions across all {@link Graph} objects.
     */
    public void rollbackAll() {
        graphs.entrySet().forEach(e -> {
            final Graph g = e.getValue();
            if (g.features().graph().supportsTransactions())
                g.tx().rollback();
        });
    }

    /**
     * Commit transactions across all {@link Graph} objects.
     */
    public void commitAll() {
        graphs.entrySet().forEach(e -> {
            final Graph g = e.getValue();
            if (g.features().graph().supportsTransactions())
                g.tx().commit();
        });
    }
}