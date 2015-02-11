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
package com.apache.tinkerpop.gremlin.groovy;

import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class SecurityCustomizerProvider implements CompilerCustomizerProvider {

    private final List<GroovyInterceptor> interceptors;

    public SecurityCustomizerProvider() {
        this.interceptors = new ArrayList<>();
    }

    public SecurityCustomizerProvider(final GroovyInterceptor... interceptors) {
        this.interceptors = Arrays.asList(interceptors);
    }

    public void addInterceptor(final GroovyInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public void removeInterceptor(final GroovyInterceptor interceptor) {
        this.interceptors.remove(interceptor);
    }

    @Override
    public CompilationCustomizer getCompilationCustomizer() {
        return new SandboxTransformer();
    }

    public void registerInterceptors() {
        interceptors.forEach(GroovyInterceptor::register);
    }

    public void unregisterInterceptors() {
        interceptors.forEach(GroovyInterceptor::unregister);
    }


}
