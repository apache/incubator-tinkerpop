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
package com.apache.tinkerpop.gremlin.process.computer.util;

import com.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import com.apache.tinkerpop.gremlin.process.computer.MapReduce;
import com.apache.tinkerpop.gremlin.process.computer.Memory;
import com.apache.tinkerpop.gremlin.process.computer.VertexProgram;
import com.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class MapMemory implements Memory.Admin, Serializable {

    private long runtime = 0l;
    private int iteration = -1;
    private final Map<String, Object> memoryMap = new HashMap<>();
    private final Set<String> memoryComputeKeys = new HashSet<>();

    public void addVertexProgramMemoryComputeKeys(final VertexProgram<?> vertexProgram) {
        this.memoryComputeKeys.addAll(vertexProgram.getMemoryComputeKeys());
    }

    public void addMapReduceMemoryKey(final MapReduce mapReduce) {
        this.memoryComputeKeys.add(mapReduce.getMemoryKey());
    }

    @Override
    public Set<String> keys() {
        return this.memoryMap.keySet();
    }

    @Override
    public <R> R get(final String key) throws IllegalArgumentException {
        final R r = (R) this.memoryMap.get(key);
        if (null == r)
            throw Memory.Exceptions.memoryDoesNotExist(key);
        else
            return r;
    }

    @Override
    public void set(final String key, Object value) {
        this.memoryMap.put(key, value);
    }

    @Override
    public int getIteration() {
        return this.iteration;
    }

    @Override
    public long getRuntime() {
        return this.runtime;
    }

    @Override
    public long incr(final String key, final long delta) {
        this.checkKeyValue(key, delta);
        if (this.memoryMap.containsKey(key)) {
            final long newValue = (long) this.memoryMap.get(key) + delta;
            this.memoryMap.put(key, newValue);
            return newValue;
        } else {
            this.memoryMap.put(key, delta);
            return delta;
        }
    }

    @Override
    public boolean and(final String key, final boolean bool) {
        this.checkKeyValue(key, bool);
        if (this.memoryMap.containsKey(key)) {
            final boolean newValue = (boolean) this.memoryMap.get(key) && bool;
            this.memoryMap.put(key, newValue);
            return newValue;
        } else {
            this.memoryMap.put(key, bool);
            return bool;
        }
    }

    @Override
    public boolean or(final String key, final boolean bool) {
        this.checkKeyValue(key, bool);
        if (this.memoryMap.containsKey(key)) {
            final boolean newValue = (boolean) this.memoryMap.get(key) || bool;
            this.memoryMap.put(key, newValue);
            return newValue;
        } else {
            this.memoryMap.put(key, bool);
            return bool;
        }
    }

    @Override
    public String toString() {
        return StringFactory.memoryString(this);
    }

    @Override
    public void incrIteration() {
        this.iteration = this.iteration + 1;
    }

    @Override
    public void setIteration(final int iteration) {
        this.iteration = iteration;
    }

    @Override
    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    private final void checkKeyValue(final String key, final Object value) {
        if (!this.memoryComputeKeys.contains(key))
            throw GraphComputer.Exceptions.providedKeyIsNotAMemoryComputeKey(key);
        MemoryHelper.validateValue(value);
    }
}
