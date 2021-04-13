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
package org.apache.tinkerpop.gremlin.util.function;

import org.apache.tinkerpop.gremlin.process.traversal.step.map.StdevGlobalStep;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author Junshi Guo
 */
public final class StdevNumberSupplier implements Supplier<StdevGlobalStep.StdevNumber>, Serializable {

    private static final StdevNumberSupplier INSTANCE = new StdevNumberSupplier();

    private StdevNumberSupplier() {}

    @Override
    public StdevGlobalStep.StdevNumber get() {
        return new StdevGlobalStep.StdevNumber();
    }

    public static StdevNumberSupplier instance() {
        return INSTANCE;
    }
}
