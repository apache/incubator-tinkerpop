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
package org.apache.tinkerpop.gremlin.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.server.channel.HttpChannelizer;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONTokens;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for server-side settings and processing.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinServerHttpIntegrateTest extends AbstractGremlinServerIntegrationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public TestName name = new TestName();

    /**
     * Configure specific Gremlin Server settings for specific tests.
     */
    @Override
    public Settings overrideSettings(final Settings settings) {
        settings.channelizer = HttpChannelizer.class.getName();
        final String nameOfTest = name.getMethodName();
        switch (nameOfTest) {
            case "should200OnGETWithGremlinQueryStringArgumentWithIteratorResult":
            case "should200OnPOSTWithGremlinJsonEndcodedBodyWithIteratorResult":
            case "should200OnPOSTWithGremlinJsonEndcodedBodyWithIteratorResultAndRebinding":
            case "should200OnGETWithGremlinQueryStringArgumentWithIteratorResultAndRebinding":
                settings.scriptEngines.get("gremlin-groovy").scripts = Arrays.asList("scripts/generate-classic.groovy");
                break;
            case "should200OnPOSTTransactionalGraph":
                deleteDirectory(new File("/tmp/neo4j"));
                settings.graphs.put("graph", "conf/neo4j-empty.properties");
                break;
        }
        return settings;
    }

    @Test
    public void should200OnGETWithGremlinQueryStringArgumentWithBindingsAndFunction() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=addItUp(Integer.parseInt(x),Integer.parseInt(y))&bindings.x=10&bindings.y=10");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(20, node.get("result").get("data").get(0).intValue());
        }
    }

    @Test
    public void should200OnGETWithGremlinQueryStringArgumentWithIteratorResult() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=g.V()");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(6, node.get("result").get("data").size());
        }
    }

    @Test
    public void should200OnGETWithGremlinQueryStringArgumentWithIteratorResultAndRebinding() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=g1.V()&rebindings.g1=g");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(6, node.get("result").get("data").size());
        }
    }

    @Test
    public void should200OnGETWithGremlinQueryStringArgument() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=1-1");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(0, node.get("result").get("data").intValue());
        }
    }

    @Test
    public void should200OnGETWithGremlinQueryStringArgumentReturningVertex() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=graph.addVertex('name','stephen')");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals("stephen", node.get("result").get("data").get(0).get("properties").get("name").get(0).get(GraphSONTokens.VALUE).asText());
        }
    }

    @Test
    public void should200OnGETWithGremlinQueryStringArgumentWithBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=Integer.parseInt(x)%2BInteger.parseInt(y)&bindings.x=10&bindings.y=10");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(20, node.get("result").get("data").get(0).intValue());
        }
    }

    @Test
    public void should400OnGETWithNoGremlinQueryStringArgument() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void should200OnGETWithAnyAcceptHeaderDefaultResultToJson() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=1-1");
        httpget.addHeader("Accept", "*/*");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(0, node.get("result").get("data").get(0).asInt());
        }
    }

    @Test
    public void should400OnGETWithBadAcceptHeader() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=1-1");
        httpget.addHeader("Accept", "application/json+something-else-that-does-not-exist");

        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBody() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"1-1\"}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(0, node.get("result").get("data").get(0).intValue());
        }
    }

    @Test
    public void should200OnPOSTTransactionalGraph() throws Exception {
        assumeNeo4jIsPresent();

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"graph.addVertex('name','stephen');g.V().count()\"}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(1, node.get("result").get("data").get(0).intValue());
        }

        final HttpGet httpget = new HttpGet("http://localhost:8182?gremlin=g.V().count()");
        httpget.addHeader("Accept", "application/json");

        // execute this a bunch of times so that there's a good chance a different thread on the server processes
        // the request
        for (int ix = 0; ix < 100; ix++) {
            try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals("application/json", response.getEntity().getContentType().getValue());
                final String json = EntityUtils.toString(response.getEntity());
                final JsonNode node = mapper.readTree(json);
                assertEquals(1, node.get("result").get("data").get(0).intValue());
            }
        }

    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyWithIteratorResult() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"g.V()\"}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(6, node.get("result").get("data").size());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyWithIteratorResultAndRebinding() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"g1.V()\",\"rebindings\":{\"g1\":\"g\"}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(6, node.get("result").get("data").size());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x+y\", \"bindings\":{\"x\":10, \"y\":10}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(20, node.get("result").get("data").get(0).intValue());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndLongBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x\", \"bindings\":{\"x\":10}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(10, node.get("result").get("data").get(0).intValue());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndDoubleBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x\", \"bindings\":{\"x\":10.5}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(10.5d, node.get("result").get("data").get(0).doubleValue(), 0.0001);
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndStringBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x\", \"bindings\":{\"x\":\"10\"}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals("10", node.get("result").get("data").get(0).textValue());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndBooleanBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x\", \"bindings\":{\"x\":true}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(true, node.get("result").get("data").get(0).booleanValue());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndNullBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x\", \"bindings\":{\"x\":null}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(true, node.get("result").get("data").get(0).isNull());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndArrayBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x\", \"bindings\":{\"x\":[1,2,3]}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(true, node.get("result").get("data").isArray());
            assertEquals(1, node.get("result").get("data").get(0).intValue());
            assertEquals(2, node.get("result").get("data").get(1).intValue());
            assertEquals(3, node.get("result").get("data").get(2).intValue());
        }
    }

    @Test
    public void should200OnPOSTWithGremlinJsonEndcodedBodyAndMapBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x\", \"bindings\":{\"x\":{\"y\":1}}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(true, node.get("result").get("data").get(0).isObject());
            assertEquals(1, node.get("result").get("data").get(0).get("y").asInt());
        }
    }

    @Test
    public void should400OnPOSTWithGremlinJsonEndcodedBodyAndBadBindings() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"x+y\", \"bindings\":10}}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void should400OnPOSTWithGremlinJsonEndcodedBodyWithNoGremlinKey() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremadfadflin\":\"1-1\"}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void should400OnPOSTWithBadAcceptHeader() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.addHeader("Accept", "application/json+something-else-that-does-not-exist");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"1-1\"}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(400, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void should200OnPOSTWithAnyAcceptHeaderDefaultResultToJson() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.addHeader("Accept", "*/*");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"1-1\"}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("application/json", response.getEntity().getContentType().getValue());
            final String json = EntityUtils.toString(response.getEntity());
            final JsonNode node = mapper.readTree(json);
            assertEquals(0, node.get("result").get("data").get(0).asInt());
        }
    }

    @Test
    public void should500OnGETWithGremlinEvalFailure() throws Exception {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost("http://localhost:8182");
        httppost.addHeader("Content-Type", "application/json");
        httppost.setEntity(new StringEntity("{\"gremlin\":\"1/0\"}", Consts.UTF_8));

        try (final CloseableHttpResponse response = httpclient.execute(httppost)) {
            assertEquals(500, response.getStatusLine().getStatusCode());
        }
    }
}
