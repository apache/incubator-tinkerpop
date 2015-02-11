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
package com.tinkerpop.gremlin.groovy.engine;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.LoadGraphWith;
import com.tinkerpop.gremlin.TestHelper;
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngineTest;
import com.tinkerpop.gremlin.structure.Graph;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.Test;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinExecutorTest extends AbstractGremlinTest {
    public static Map<String, String> PATHS = new HashMap<>();
    private final BasicThreadFactory testingThreadFactory = new BasicThreadFactory.Builder().namingPattern("test-gremlin-executor-%d").build();

    static {
        try {
            final List<String> groovyScriptResources = Arrays.asList("GremlinExecutorInit.groovy");
            for (final String fileName : groovyScriptResources) {
                PATHS.put(fileName, TestHelper.generateTempFileFromResource(GremlinExecutorTest.class, fileName, "").getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldEvalScript() throws Exception {
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().create();
        assertEquals(2, gremlinExecutor.eval("1+1").get());
        gremlinExecutor.close();
    }

    @Test
    public void shouldEvalMultipleScripts() throws Exception {
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().create();
        assertEquals(2, gremlinExecutor.eval("1+1").get());
        assertEquals(3, gremlinExecutor.eval("1+2").get());
        assertEquals(4, gremlinExecutor.eval("1+3").get());
        assertEquals(5, gremlinExecutor.eval("1+4").get());
        assertEquals(6, gremlinExecutor.eval("1+5").get());
        assertEquals(7, gremlinExecutor.eval("1+6").get());
        gremlinExecutor.close();
    }

    @Test
    public void shouldEvalScriptWithBindings() throws Exception {
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().create();
        final Bindings b = new SimpleBindings();
        b.put("x", 1);
        assertEquals(2, gremlinExecutor.eval("1+x", b).get());
        gremlinExecutor.close();
    }

    @Test
    public void shouldEvalScriptWithGlobalBindings() throws Exception {
        final Bindings b = new SimpleBindings();
        b.put("x", 1);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().globalBindings(b).create();
        assertEquals(2, gremlinExecutor.eval("1+x").get());
        gremlinExecutor.close();
    }

    @Test
    public void shouldEvalScriptWithGlobalAndLocalBindings() throws Exception {
        final Bindings g = new SimpleBindings();
        g.put("x", 1);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().globalBindings(g).create();
        final Bindings b = new SimpleBindings();
        b.put("y", 1);
        assertEquals(2, gremlinExecutor.eval("y+x", b).get());
        gremlinExecutor.close();
    }

    @Test
    public void shouldEvalScriptWithLocalOverridingGlobalBindings() throws Exception {
        final Bindings g = new SimpleBindings();
        g.put("x", 1);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().globalBindings(g).create();
        final Bindings b = new SimpleBindings();
        b.put("x", 10);
        assertEquals(11, gremlinExecutor.eval("x+1", b).get());
        gremlinExecutor.close();
    }

    @Test
    public void shouldTimeoutScript() throws Exception {
        final AtomicBoolean successCalled = new AtomicBoolean(false);
        final AtomicBoolean failureCalled = new AtomicBoolean(false);

        final CountDownLatch timeOutCount = new CountDownLatch(1);

        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .scriptEvaluationTimeout(250)
                .afterFailure((b, e) -> failureCalled.set(true))
                .afterSuccess((b) -> successCalled.set(true))
                .afterTimeout((b) -> timeOutCount.countDown()).create();
        try {
            gremlinExecutor.eval("Thread.sleep(1000);10").get();
            fail("This script should have timed out with an exception");
        } catch (Exception ex) {
            assertEquals(TimeoutException.class, ex.getCause().getClass());
        }

        timeOutCount.await(2000, TimeUnit.MILLISECONDS);

        assertFalse(successCalled.get());
        assertFalse(failureCalled.get());
        assertEquals(0, timeOutCount.getCount());
        gremlinExecutor.close();
    }

    @Test
    public void shouldCallFail() throws Exception {
        final AtomicBoolean timeoutCalled = new AtomicBoolean(false);
        final AtomicBoolean successCalled = new AtomicBoolean(false);
        final AtomicBoolean failureCalled = new AtomicBoolean(false);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .afterFailure((b, e) -> failureCalled.set(true))
                .afterSuccess((b) -> successCalled.set(true))
                .afterTimeout((b) -> timeoutCalled.set(true)).create();
        try {
            gremlinExecutor.eval("10/0").get();
            fail();
        } catch (Exception ex) {

        }

        // need to wait long enough for the script to complete
        Thread.sleep(750);

        assertFalse(timeoutCalled.get());
        assertFalse(successCalled.get());
        assertTrue(failureCalled.get());
        gremlinExecutor.close();
    }

    @Test
    public void shouldCallSuccess() throws Exception {
        final AtomicBoolean timeoutCalled = new AtomicBoolean(false);
        final AtomicBoolean successCalled = new AtomicBoolean(false);
        final AtomicBoolean failureCalled = new AtomicBoolean(false);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .afterFailure((b, e) -> failureCalled.set(true))
                .afterSuccess((b) -> successCalled.set(true))
                .afterTimeout((b) -> timeoutCalled.set(true)).create();
        assertEquals(2, gremlinExecutor.eval("1+1").get());

        // need to wait long enough for the script to complete
        Thread.sleep(750);

        assertFalse(timeoutCalled.get());
        assertTrue(successCalled.get());
        assertFalse(failureCalled.get());
        gremlinExecutor.close();
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldAllowTraversalToIterateInDifferentThreadThanOriginallyEvaluatedWithAutoCommit() throws Exception {
        // this test sort of simulates Gremlin Server interaction where a Traversal is eval'd in one Thread, but
        // then iterated in another.  note that Gremlin Server configures the script engine to auto-commit
        // after evaluation.  this basically tests the state of the Gremlin Server GremlinExecutor when
        // being used in sessionless mode
        final ExecutorService evalExecutor = Executors.newSingleThreadExecutor(testingThreadFactory);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .afterSuccess(b -> {
                    final Graph graph = (Graph) b.get("g");
                    if (graph.features().graph().supportsTransactions())
                        graph.tx().commit();
                })
                .executorService(evalExecutor).create();

        final Map<String,Object> bindings = new HashMap<>();
        bindings.put("g", g);

        final AtomicInteger vertexCount = new AtomicInteger(0);

        final ExecutorService iterationExecutor = Executors.newSingleThreadExecutor(testingThreadFactory);
        gremlinExecutor.eval("g.V().out()", bindings).thenAcceptAsync(o -> {
            final Iterator itty = (Iterator) o;
            itty.forEachRemaining(v -> vertexCount.incrementAndGet());
        }, iterationExecutor).join();

        assertEquals(6, vertexCount.get());

        gremlinExecutor.close();
        evalExecutor.shutdown();
        evalExecutor.awaitTermination(30000, TimeUnit.MILLISECONDS);
        iterationExecutor.shutdown();
        iterationExecutor.awaitTermination(30000, TimeUnit.MILLISECONDS);
    }

    @Test
    @LoadGraphWith(LoadGraphWith.GraphData.MODERN)
    public void shouldAllowTraversalToIterateInDifferentThreadThanOriginallyEvaluatedWithoutAutoCommit() throws Exception {
        // this test sort of simulates Gremlin Server interaction where a Traversal is eval'd in one Thread, but
        // then iterated in another.  this basically tests the state of the Gremlin Server GremlinExecutor when
        // being used in session mode
        final ExecutorService evalExecutor = Executors.newSingleThreadExecutor(testingThreadFactory);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().executorService(evalExecutor).create();

        final Map<String,Object> bindings = new HashMap<>();
        bindings.put("g", g);

        final AtomicInteger vertexCount = new AtomicInteger(0);

        final ExecutorService iterationExecutor = Executors.newSingleThreadExecutor(testingThreadFactory);
        gremlinExecutor.eval("g.V().out()", bindings).thenAcceptAsync(o -> {
            final Iterator itty = (Iterator) o;
            itty.forEachRemaining(v -> vertexCount.incrementAndGet());
        }, iterationExecutor).join();

        assertEquals(6, vertexCount.get());

        gremlinExecutor.close();
        evalExecutor.shutdown();
        evalExecutor.awaitTermination(30000, TimeUnit.MILLISECONDS);
        iterationExecutor.shutdown();
        iterationExecutor.awaitTermination(30000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void shouldEvalInMultipleThreads() throws Exception {
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().create();

        final CyclicBarrier barrier = new CyclicBarrier(2);
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicBoolean b1 = new AtomicBoolean(false);
        final Thread t1 = new Thread(() -> {
            try {
                barrier.await();
                i1.set((Integer) gremlinExecutor.eval("1+1").get());
            } catch (Exception ex) {
                b1.set(true);
            }
        });

        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicBoolean b2 = new AtomicBoolean(false);
        final Thread t2 = new Thread(() -> {
            try {
                barrier.await();
                i2.set((Integer) gremlinExecutor.eval("1+1").get());
            } catch (Exception ex) {
                b2.set(true);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertEquals(2, i1.get());
        assertEquals(2, i2.get());
        assertFalse(b1.get());
        assertFalse(b2.get());

        gremlinExecutor.close();

    }

    @Test
    public void shouldNotExhaustThreads() throws Exception {
        // this is not representative of how the GremlinExecutor should be configured.  A single thread executor
        // shared will create odd behaviors, but it's good for this test.
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(testingThreadFactory);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .executorService(executorService)
                .scheduledExecutorService(executorService).create();

        final AtomicInteger count = new AtomicInteger(0);
        assertTrue(IntStream.range(0, 1000).mapToObj(i -> gremlinExecutor.eval("1+1")).allMatch(f -> {
            try {
                return (Integer) f.get() == 2;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                count.incrementAndGet();
            }
        }));

        assertEquals(1000, count.intValue());

        executorService.shutdown();
        executorService.awaitTermination(30000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void shouldFailUntilImportExecutes() throws Exception {
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build().create();

        final Set<String> imports = new HashSet<String>() {{
            add("import java.awt.Color");
        }};

        final AtomicInteger successes = new AtomicInteger(0);
        final AtomicInteger failures = new AtomicInteger(0);

        // issue 1000 scripts in one thread using a class that isn't imported.  this will result in failure.
        // while that thread is running start a new thread that issues an addImports to include that class.
        // this should block further evals in the first thread until the import is complete at which point
        // evals in the first thread will resume and start to succeed
        final Thread t1 = new Thread(() ->
                IntStream.range(0, 1000).mapToObj(i -> gremlinExecutor.eval("Color.BLACK"))
                        .forEach(f -> {
                            f.exceptionally(t -> failures.incrementAndGet()).join();
                            if (!f.isCompletedExceptionally())
                                successes.incrementAndGet();
                        })
        );

        final Thread t2 = new Thread(() -> {
            while (failures.get() < 500) {
            }
            gremlinExecutor.getScriptEngines().addImports(imports);
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertTrue(successes.intValue() > 0);
        assertTrue(failures.intValue() >= 500);

        gremlinExecutor.close();
    }

    @Test
    public void shouldInitializeWithScript() throws Exception {
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .addEngineSettings("gremlin-groovy",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Arrays.asList(PATHS.get("GremlinExecutorInit.groovy")),
                        Collections.emptyMap())
                .create();

        assertEquals(2, gremlinExecutor.eval("add(1,1)").get());

        gremlinExecutor.close();
    }

    @Test
    public void shouldSecureAll() throws Exception {
        GroovyInterceptor.getApplicableInterceptors().forEach(GroovyInterceptor::unregister);
        final Map<String, Object> config = new HashMap<>();
        config.put("sandbox", GremlinGroovyScriptEngineTest.DenyAll.class.getName());
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .addEngineSettings("gremlin-groovy",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Arrays.asList(PATHS.get("GremlinExecutorInit.groovy")),
                        config)
                .create();
        try {
            gremlinExecutor.eval("c = new java.awt.Color(255, 255, 255)").get();
            fail("Should have failed security");
        } catch (Exception se) {
            assertEquals(SecurityException.class, se.getCause().getCause().getCause().getCause().getClass());
        } finally {
            gremlinExecutor.close();
        }
    }

    @Test
    public void shouldSecureSome() throws Exception {
        GroovyInterceptor.getApplicableInterceptors().forEach(GroovyInterceptor::unregister);
        final Map<String, Object> config = new HashMap<>();
        config.put("sandbox", GremlinGroovyScriptEngineTest.AllowSome.class.getName());
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .addEngineSettings("gremlin-groovy",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Arrays.asList(PATHS.get("GremlinExecutorInit.groovy")),
                        config)
                .create();
        try {
            gremlinExecutor.eval("c = 'new java.awt.Color(255, 255, 255)'").get();
            fail("Should have failed security");
        } catch (Exception se) {
            assertEquals(SecurityException.class, se.getCause().getCause().getCause().getCause().getClass());
        }

        try {
            final java.awt.Color c = (java.awt.Color) gremlinExecutor.eval("g = new java.awt.Color(255, 255, 255)").get();
            assertEquals(java.awt.Color.class, c.getClass());
        } catch (Exception ignored) {
            fail("Should not have tossed an exception");
        } finally {
            gremlinExecutor.close();
        }
    }

    @Test
    public void shouldInitializeWithScriptAndWorkAfterReset() throws Exception {
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .addEngineSettings("gremlin-groovy",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Arrays.asList(PATHS.get("GremlinExecutorInit.groovy")),
                        Collections.emptyMap())
                .create();

        assertEquals(2, gremlinExecutor.eval("add(1,1)").get());

        gremlinExecutor.getScriptEngines().reset();

        assertEquals(2, gremlinExecutor.eval("add(1,1)").get());

        gremlinExecutor.close();
    }

    @Test
    public void shouldNotShutdownExecutorServicesSuppliedToGremlinExecutor() throws Exception {
        final ScheduledExecutorService service = Executors.newScheduledThreadPool(4, testingThreadFactory);
        final GremlinExecutor gremlinExecutor = GremlinExecutor.build()
                .executorService(service)
                .scheduledExecutorService(service).create();

        gremlinExecutor.close();
        assertFalse(service.isShutdown());
        service.shutdown();
        service.awaitTermination(30000, TimeUnit.MILLISECONDS);
    }
}
