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

import com.codahale.metrics.Meter;
import com.apache.tinkerpop.gremlin.driver.message.RequestMessage;
import com.apache.tinkerpop.gremlin.driver.message.ResponseMessage;
import com.apache.tinkerpop.gremlin.driver.message.ResponseStatusCode;
import com.apache.tinkerpop.gremlin.groovy.engine.GremlinExecutor;
import com.apache.tinkerpop.gremlin.server.Context;
import com.apache.tinkerpop.gremlin.server.Graphs;
import com.apache.tinkerpop.gremlin.server.GremlinServer;
import com.apache.tinkerpop.gremlin.server.OpProcessor;
import com.apache.tinkerpop.gremlin.server.Settings;
import com.apache.tinkerpop.gremlin.server.op.OpLoader;
import com.apache.tinkerpop.gremlin.server.op.OpProcessorException;
import com.apache.tinkerpop.gremlin.server.util.MetricManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@ChannelHandler.Sharable
public class OpSelectorHandler extends MessageToMessageDecoder<RequestMessage> {
    private static final Logger logger = LoggerFactory.getLogger(OpSelectorHandler.class);
    static final Meter errorMeter = MetricManager.INSTANCE.getMeter(name(GremlinServer.class, "errors"));

    private final Settings settings;
    private final Graphs graphs;

    private final GremlinExecutor gremlinExecutor;
    private final ScheduledExecutorService scheduledExecutorService;

    public OpSelectorHandler(final Settings settings, final Graphs graphs, final GremlinExecutor gremlinExecutor,
                             final ScheduledExecutorService scheduledExecutorService) {
        this.settings = settings;
        this.graphs = graphs;
        this.gremlinExecutor = gremlinExecutor;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    protected void decode(final ChannelHandlerContext channelHandlerContext, final RequestMessage msg,
                          final List<Object> objects) throws Exception {
        final Context gremlinServerContext = new Context(msg, channelHandlerContext, settings,
                graphs, gremlinExecutor, this.scheduledExecutorService);
        try {
            // choose a processor to do the work based on the request message.
            final Optional<OpProcessor> processor = OpLoader.getProcessor(msg.getProcessor());

            if (processor.isPresent())
                // the processor is known so use it to evaluate the message
                objects.add(Pair.with(msg, processor.get().select(gremlinServerContext)));
            else {
                // invalid op processor selected so write back an error by way of OpProcessorException.
                final String errorMessage = String.format("Invalid OpProcessor requested [%s]", msg.getProcessor());
                throw new OpProcessorException(errorMessage, ResponseMessage.build(msg).code(ResponseStatusCode.REQUEST_ERROR_INVALID_REQUEST_ARGUMENTS).result(errorMessage).create());
            }
        } catch (OpProcessorException ope) {
            errorMeter.mark();
            logger.warn(ope.getMessage(), ope);
            channelHandlerContext.writeAndFlush(ope.getResponseMessage());
        }
    }
}
