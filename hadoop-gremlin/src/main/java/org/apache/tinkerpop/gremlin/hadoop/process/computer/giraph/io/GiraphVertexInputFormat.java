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
package com.tinkerpop.gremlin.hadoop.process.computer.giraph.io;

import com.tinkerpop.gremlin.hadoop.Constants;
import com.tinkerpop.gremlin.hadoop.structure.io.VertexWritable;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GiraphVertexInputFormat extends VertexInputFormat {

    private InputFormat<NullWritable, VertexWritable> hadoopGraphInputFormat;

    @Override
    public List<InputSplit> getSplits(final JobContext context, final int minSplitCountHint) throws IOException, InterruptedException {
        this.constructor(context.getConfiguration());
        return this.hadoopGraphInputFormat.getSplits(context);
    }

    @Override
    public VertexReader createVertexReader(final InputSplit split, final TaskAttemptContext context) throws IOException {
        this.constructor(context.getConfiguration());
        try {
            return new GiraphVertexReader(this.hadoopGraphInputFormat.createRecordReader(split, context));
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private final void constructor(final Configuration configuration) {
        if (null == this.hadoopGraphInputFormat) {
            this.hadoopGraphInputFormat = ReflectionUtils.newInstance(configuration.getClass(Constants.GREMLIN_HADOOP_GRAPH_INPUT_FORMAT, InputFormat.class, InputFormat.class), configuration);
        }
    }

}
