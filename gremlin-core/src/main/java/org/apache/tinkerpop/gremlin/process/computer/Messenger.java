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
package com.tinkerpop.gremlin.process.computer;

/**
 * The {@link Messenger} serves as the routing system for messages between vertices. For distributed systems,
 * the messenger can implement a "message passing" engine (distributed memory). For single machine systems, the
 * messenger can implement a "state sharing" engine (shared memory). Each messenger is tied to the particular
 * vertex distributing the message.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public interface Messenger<M> {

    /**
     * The currently executing vertex can receive the messages of the provided {@link MessageScope}.
     *
     * @param messageScope the message scope of the messages to receive
     * @return the messages for that vertex
     */
    public Iterable<M> receiveMessages(final MessageScope messageScope);

    /**
     * The currently executing vertex can send a message with provided {@link MessageScope}.
     *
     * @param messageScope the message scope of the message being sent
     * @param message      the message to send
     */
    public void sendMessage(final MessageScope messageScope, final M message);

}
