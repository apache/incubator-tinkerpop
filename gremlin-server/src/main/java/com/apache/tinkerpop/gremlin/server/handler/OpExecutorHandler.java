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
package com.apache.tinkerpop.gremlin.server.handler;

import com.apache.tinkerpop.gremlin.driver.message.RequestMessage;
import com.apache.tinkerpop.gremlin.groovy.engine.GremlinExecutor;
import com.apache.tinkerpop.gremlin.server.Context;
import com.apache.tinkerpop.gremlin.server.Graphs;
import com.apache.tinkerpop.gremlin.server.Settings;
import com.apache.tinkerpop.gremlin.server.op.OpProcessorException;
import com.apache.tinkerpop.gremlin.util.function.ThrowingConsumer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@ChannelHandler.Sharable
public class OpExecutorHandler extends SimpleChannelInboundHandler<Pair<RequestMessage, ThrowingConsumer<Context>>> {
    private static final Logger logger = LoggerFactory.getLogger(OpExecutorHandler.class);

    private final Settings settings;
    private final Graphs graphs;
    private final ScheduledExecutorService scheduledExecutorService;
    private final GremlinExecutor gremlinExecutor;

    public OpExecutorHandler(final Settings settings, final Graphs graphs, final GremlinExecutor gremlinExecutor,
                             final ScheduledExecutorService scheduledExecutorService) {
        this.settings = settings;
        this.graphs = graphs;
        this.gremlinExecutor = gremlinExecutor;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final Pair<RequestMessage, ThrowingConsumer<Context>> objects) throws Exception {
        final RequestMessage msg = objects.getValue0();
        final ThrowingConsumer<Context> op = objects.getValue1();
        final Context gremlinServerContext = new Context(msg, channelHandlerContext,
                settings, graphs, gremlinExecutor, scheduledExecutorService);

        try {
            op.accept(gremlinServerContext);
        } catch (OpProcessorException ope) {
            // Ops may choose to throw OpProcessorException or write the error ResponseMessage down the line
            // themselves
            logger.warn(ope.getMessage(), ope);
            channelHandlerContext.writeAndFlush(ope.getResponseMessage());
        } finally {
            ReferenceCountUtil.release(objects);
        }
    }
}
