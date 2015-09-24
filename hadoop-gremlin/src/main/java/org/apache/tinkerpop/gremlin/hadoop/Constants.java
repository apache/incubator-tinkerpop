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
package org.apache.tinkerpop.gremlin.hadoop;

import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class Constants {

    private Constants() {
    }

    public static final String GREMLIN_HADOOP_INPUT_LOCATION = "gremlin.hadoop.inputLocation";
    public static final String GREMLIN_HADOOP_OUTPUT_LOCATION = "gremlin.hadoop.outputLocation";
    public static final String GREMLIN_HADOOP_GRAPH_INPUT_FORMAT = "gremlin.hadoop.graphInputFormat";
    public static final String GREMLIN_HADOOP_GRAPH_OUTPUT_FORMAT = "gremlin.hadoop.graphOutputFormat";
    public static final String GREMLIN_HADOOP_GRAPH_INPUT_FORMAT_HAS_EDGES = "gremlin.hadoop.graphOutputFormat.hasEdges";
    public static final String GREMLIN_HADOOP_GRAPH_OUTPUT_FORMAT_HAS_EDGES = "gremlin.hadoop.graphInputFormat.hasEdges";;

    public static final String GREMLIN_HADOOP_JARS_IN_DISTRIBUTED_CACHE = "gremlin.hadoop.jarsInDistributedCache";
    public static final String HIDDEN_G = Graph.Hidden.hide("g");
    public static final String GREMLIN_HADOOP_JOB_PREFIX = "HadoopGremlin: ";
    public static final String GREMLIN_HADOOP_GIRAPH_JOB_PREFIX = "HadoopGremlin(Giraph): ";
    // public static final String GREMLIN_HADOOP_MAP_REDUCE_JOB_PREFIX = "HadoopGremlin(MapReduce): ";
    public static final String GREMLIN_HADOOP_SPARK_JOB_PREFIX = "HadoopGremlin(Spark): ";
    public static final String HADOOP_GREMLIN_LIBS = "HADOOP_GREMLIN_LIBS";
    public static final String DOT_JAR = ".jar";
    public static final String HIDDEN_ITERATION = Graph.Hidden.hide("gremlin.hadoop.iteration");
    public static final String GREMLIN_HADOOP_MAP_REDUCE_CLASS = "gremlin.hadoop.mapReduceClass";

    public static final String MAPREDUCE_INPUT_FILEINPUTFORMAT_INPUTDIR = "mapreduce.input.fileinputformat.inputdir";

    // spark based constants
    public static final String GREMLIN_HADOOP_GRAPH_INPUT_RDD = "gremlin.hadoop.graphInputRDD";
    public static final String GREMLIN_HADOOP_GRAPH_OUTPUT_RDD = "gremlin.hadoop.graphOutputRDD";
}
