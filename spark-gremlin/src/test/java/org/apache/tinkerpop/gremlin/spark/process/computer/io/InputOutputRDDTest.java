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
package org.apache.tinkerpop.gremlin.spark.process.computer.io;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.hadoop.Constants;
import org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.computer.traversal.TraversalVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.engine.ComputerTraversalEngine;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.junit.Test;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class InputOutputRDDTest {

    @Test
    public void shouldReadFromWriteToArbitraryRDD() throws Exception {
        final Configuration configuration = new BaseConfiguration();
        configuration.setProperty("spark.master", "local[4]");
        configuration.setProperty("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        configuration.setProperty(Graph.GRAPH, HadoopGraph.class.getName());
        configuration.setProperty(Constants.GREMLIN_HADOOP_GRAPH_INPUT_RDD, ExampleInputRDD.class.getCanonicalName());
        configuration.setProperty(Constants.GREMLIN_HADOOP_GRAPH_OUTPUT_RDD, ExampleOutputRDD.class.getCanonicalName());
        configuration.setProperty(Constants.GREMLIN_HADOOP_OUTPUT_LOCATION, "target/test-output");
        configuration.setProperty(Constants.GREMLIN_HADOOP_JARS_IN_DISTRIBUTED_CACHE, false);
        ////////
        Graph graph = GraphFactory.open(configuration);
        graph.compute(SparkGraphComputer.class)
                .result(GraphComputer.ResultGraph.NEW)
                .persist(GraphComputer.Persist.EDGES)
                .program(TraversalVertexProgram.build()
                        .traversal(GraphTraversalSource.build().engine(ComputerTraversalEngine.build().computer(SparkGraphComputer.class)),
                                "gremlin-groovy",
                                "g.V()").create(graph)).submit().get();
    }
}
