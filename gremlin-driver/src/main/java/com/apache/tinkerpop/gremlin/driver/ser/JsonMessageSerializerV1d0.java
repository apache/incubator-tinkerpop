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
package com.apache.tinkerpop.gremlin.driver.ser;

import com.apache.tinkerpop.gremlin.driver.message.RequestMessage;
import com.apache.tinkerpop.gremlin.driver.message.ResponseMessage;
import com.apache.tinkerpop.gremlin.driver.message.ResponseStatusCode;
import com.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Serialize results to JSON with version 1.0.x schema.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class JsonMessageSerializerV1d0 extends AbstractJsonMessageSerializerV1d0 implements MessageTextSerializer {
    private static final Logger logger = LoggerFactory.getLogger(JsonMessageSerializerV1d0.class);
    private static final String MIME_TYPE = SerTokens.MIME_JSON;

    private static byte[] header;

    static {
        final ByteBuffer buffer = ByteBuffer.allocate(MIME_TYPE.length() + 1);
        buffer.put((byte) MIME_TYPE.length());
        buffer.put(MIME_TYPE.getBytes());
        header = buffer.array();
    }

    public JsonMessageSerializerV1d0() {
        super();
    }

    public JsonMessageSerializerV1d0(final GraphSONMapper mapper) {
        super(mapper);
    }

    @Override
    public String[] mimeTypesSupported() {
        return new String[]{MIME_TYPE};
    }

    @Override
    GraphSONMapper.Builder configureBuilder(GraphSONMapper.Builder builder) {
        return builder.addCustomModule(new GremlinServerModule())
                .embedTypes(false);
    }

    @Override
    byte[] obtainHeader() {
        return header;
    }

    @Override
    public ResponseMessage deserializeResponse(final String msg) throws SerializationException {
        try {
            final Map<String, Object> responseData = mapper.readValue(msg, mapTypeReference);
            final Map<String, Object> status = (Map<String, Object>) responseData.get(SerTokens.TOKEN_STATUS);
            final Map<String, Object> result = (Map<String, Object>) responseData.get(SerTokens.TOKEN_RESULT);
            return ResponseMessage.build(UUID.fromString(responseData.get(SerTokens.TOKEN_REQUEST).toString()))
                    .code(ResponseStatusCode.getFromValue((Integer) status.get(SerTokens.TOKEN_CODE)))
                    .statusMessage(status.get(SerTokens.TOKEN_MESSAGE).toString())
                    .statusAttributes((Map<String, Object>) status.get(SerTokens.TOKEN_ATTRIBUTES))
                    .result(result.get(SerTokens.TOKEN_DATA))
                    .responseMetaData((Map<String, Object>) result.get(SerTokens.TOKEN_META))
                    .create();
        } catch (Exception ex) {
            logger.warn("Response [{}] could not be deserialized by {}.", msg, AbstractJsonMessageSerializerV1d0.class.getName());
            throw new SerializationException(ex);
        }
    }

    @Override
    public String serializeResponseAsString(final ResponseMessage responseMessage) throws SerializationException {
        try {
            final Map<String, Object> result = new HashMap<>();
            result.put(SerTokens.TOKEN_DATA, responseMessage.getResult().getData());
            result.put(SerTokens.TOKEN_META, responseMessage.getResult().getMeta());

            final Map<String, Object> status = new HashMap<>();
            status.put(SerTokens.TOKEN_MESSAGE, responseMessage.getStatus().getMessage());
            status.put(SerTokens.TOKEN_CODE, responseMessage.getStatus().getCode().getValue());
            status.put(SerTokens.TOKEN_ATTRIBUTES, responseMessage.getStatus().getAttributes());

            final Map<String, Object> message = new HashMap<>();
            message.put(SerTokens.TOKEN_STATUS, status);
            message.put(SerTokens.TOKEN_RESULT, result);
            message.put(SerTokens.TOKEN_REQUEST, responseMessage.getRequestId() != null ? responseMessage.getRequestId() : null);

            return mapper.writeValueAsString(message);
        } catch (Exception ex) {
            logger.warn("Response [{}] could not be serialized by {}.", responseMessage.toString(), AbstractJsonMessageSerializerV1d0.class.getName());
            throw new RuntimeException("Error during serialization.", ex);
        }
    }

    @Override
    public RequestMessage deserializeRequest(final String msg) throws SerializationException {
        try {
            return mapper.readValue(msg, RequestMessage.class);
        } catch (Exception ex) {
            logger.warn("Request [{}] could not be deserialized by {}.", msg, AbstractJsonMessageSerializerV1d0.class.getName());
            throw new SerializationException(ex);
        }
    }

    @Override
    public String serializeRequestAsString(final RequestMessage requestMessage) throws SerializationException {
        try {
            return mapper.writeValueAsString(requestMessage);
        } catch (Exception ex) {
            logger.warn("Request [{}] could not be serialized by {}.", requestMessage.toString(), AbstractJsonMessageSerializerV1d0.class.getName());
            throw new RuntimeException("Error during serialization.", ex);
        }
    }
}
