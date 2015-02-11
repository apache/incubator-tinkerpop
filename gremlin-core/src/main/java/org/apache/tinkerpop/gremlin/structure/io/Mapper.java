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
package org.apache.tinkerpop.gremlin.structure.io;

/**
 * Represents a low-level serialization class that can be used to map classes to serializers.  These implementation
 * create instances of serializers from other libraries (e.g. creating a {@code Kryo} instance).
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public interface Mapper<T> {
    /**
     * Create a new instance of the internal object mapper that an implementation represents.
     */
    public T createMapper();
}
