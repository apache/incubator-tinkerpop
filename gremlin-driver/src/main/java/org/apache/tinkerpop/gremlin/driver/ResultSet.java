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
package org.apache.tinkerpop.gremlin.driver;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A {@code ResultSet} is returned from the submission of a Gremlin script to the server and represents the
 * results provided by the server.  The results from the server are streamed into the {@code ResultSet} and
 * therefore may not be available immediately.  As such, {@code ResultSet} provides access to a a number
 * of functions that help to work with the asynchronous nature of the data streaming back.  Data from results
 * is stored in an {@link Result} which can be used to retrieve the item once it is on the client side.
 * <p/>
 * Note that a {@code ResultSet} is a forward-only stream only so depending on how the methods are called and
 * interacted with, it is possible to return partial bits of total response (e.g. calling {@link #one()} followed
 * by {@link #all()} will make it so that the {@link List} of results returned from {@link #all()} have one
 * {@link Result} missing from the total set as it was already retrieved by {@link #one}.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class ResultSet implements Iterable<Result> {
    private final ResultQueue resultQueue;
    private final ExecutorService executor;
    private final Channel channel;
    private final Supplier<Void> onChannelError;

    private final CompletableFuture<Void> readCompleted;

    public ResultSet(final ResultQueue resultQueue, final ExecutorService executor,
                     final Channel channel, final Supplier<Void> onChannelError,
                     final CompletableFuture<Void> readCompleted) {
        this.executor = executor;
        this.resultQueue = resultQueue;
        this.channel = channel;
        this.onChannelError = onChannelError;
        this.readCompleted = readCompleted;
    }

    /**
     * Determines if all items have been returned to the client.
     */
    public boolean allItemsAvailable() {
        return readCompleted.isDone();
    }

    /**
     * Gets the number of items available on the client.
     */
    public int getAvailableItemCount() {
        return resultQueue.size();
    }

    /**
     * Determines if there are any remaining items being streamed to the client.
     */
    public boolean isExhausted() {
        return !(!allItemsAvailable() || !resultQueue.isEmpty());
    }

    /**
     * Get the next {@link Result} from the stream, blocking until one is available.
     */
    public Result one() {
        return some(1).join().get(0);
    }

    /**
     * The returned {@link CompletableFuture} completes when the number of items specified are available.  The
     * number returned will be equal to or less than that number.  They will only be less if the stream is
     * completed and there are less than that number specified available.
     */
    public CompletableFuture<List<Result>> some(final int items) {
        return resultQueue.await(items);
    }

    /**
     * The returned {@link CompletableFuture} completes when all reads are complete for this request and the
     * entire result has been accounted for on the client. While this method is named "all" it really refers to
     * retrieving all remaining items in the set.  For large result sets it is preferred to use
     * {@link Iterator} or {@link Stream} options, as the results will be held in memory at once.
     */
    public CompletableFuture<List<Result>> all() {
        return readCompleted.thenApplyAsync(it -> {
            final List<Result> list = new ArrayList<>();
            resultQueue.drainTo(list);
            return list;
        }, executor);
    }

    /**
     * Stream items with a blocking iterator.
     */
    public Stream<Result> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.IMMUTABLE | Spliterator.SIZED), false);
    }

    @Override
    public Iterator<Result> iterator() {
        return new Iterator<Result>() {

            @Override
            public boolean hasNext() {
                return !isExhausted();
            }

            @Override
            public Result next() {
                return ResultSet.this.one();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
