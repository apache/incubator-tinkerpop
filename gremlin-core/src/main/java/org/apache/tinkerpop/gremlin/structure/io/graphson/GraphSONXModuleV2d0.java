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
package org.apache.tinkerpop.gremlin.structure.io.graphson;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Version 2.0 of GraphSON extensions.
 */
public final class GraphSONXModuleV2d0 extends GraphSONModule {

    private static final Map<Class, String> TYPE_DEFINITIONS = Collections.unmodifiableMap(
            new LinkedHashMap<Class, String>() {{
                put(ByteBuffer.class, "ByteBuffer");
                put(Short.class, "Int16");
                put(BigInteger.class, "BigInteger");
                put(BigDecimal.class, "BigDecimal");
                put(Byte.class, "Byte");
                put(Character.class, "Char");
                put(InetAddress.class, "InetAddress");

                // Time serializers/deserializers
                put(Duration.class, "Duration");
                put(Instant.class, "Instant");
                put(LocalDate.class, "LocalDate");
                put(LocalDateTime.class, "LocalDateTime");
                put(LocalTime.class, "LocalTime");
                put(MonthDay.class, "MonthDay");
                put(OffsetDateTime.class, "OffsetDateTime");
                put(OffsetTime.class, "OffsetTime");
                put(Period.class, "Period");
                put(Year.class, "Year");
                put(YearMonth.class, "YearMonth");
                put(ZonedDateTime.class, "ZonedDateTime");
                put(ZoneOffset.class, "ZoneOffset");
            }});

    /**
     * Constructs a new object.
     */
    protected GraphSONXModuleV2d0(final boolean normalize) {
        super("graphsonx-2.0");

        /////////////////////// SERIALIZERS ////////////////////////////

        // java.time
        addSerializer(Duration.class, new JavaTimeSerializersV2d0.DurationJacksonSerializer());
        addSerializer(Instant.class, new JavaTimeSerializersV2d0.InstantJacksonSerializer());
        addSerializer(LocalDate.class, new JavaTimeSerializersV2d0.LocalDateJacksonSerializer());
        addSerializer(LocalDateTime.class, new JavaTimeSerializersV2d0.LocalDateTimeJacksonSerializer());
        addSerializer(LocalTime.class, new JavaTimeSerializersV2d0.LocalTimeJacksonSerializer());
        addSerializer(MonthDay.class, new JavaTimeSerializersV2d0.MonthDayJacksonSerializer());
        addSerializer(OffsetDateTime.class, new JavaTimeSerializersV2d0.OffsetDateTimeJacksonSerializer());
        addSerializer(OffsetTime.class, new JavaTimeSerializersV2d0.OffsetTimeJacksonSerializer());
        addSerializer(Period.class, new JavaTimeSerializersV2d0.PeriodJacksonSerializer());
        addSerializer(Year.class, new JavaTimeSerializersV2d0.YearJacksonSerializer());
        addSerializer(YearMonth.class, new JavaTimeSerializersV2d0.YearMonthJacksonSerializer());
        addSerializer(ZonedDateTime.class, new JavaTimeSerializersV2d0.ZonedDateTimeJacksonSerializer());
        addSerializer(ZoneOffset.class, new JavaTimeSerializersV2d0.ZoneOffsetJacksonSerializer());

        /////////////////////// DESERIALIZERS ////////////////////////////

        // java.time
        addDeserializer(Duration.class, new JavaTimeSerializersV2d0.DurationJacksonDeserializer());
        addDeserializer(Instant.class, new JavaTimeSerializersV2d0.InstantJacksonDeserializer());
        addDeserializer(LocalDate.class, new JavaTimeSerializersV2d0.LocalDateJacksonDeserializer());
        addDeserializer(LocalDateTime.class, new JavaTimeSerializersV2d0.LocalDateTimeJacksonDeserializer());
        addDeserializer(LocalTime.class, new JavaTimeSerializersV2d0.LocalTimeJacksonDeserializer());
        addDeserializer(MonthDay.class, new JavaTimeSerializersV2d0.MonthDayJacksonDeserializer());
        addDeserializer(OffsetDateTime.class, new JavaTimeSerializersV2d0.OffsetDateTimeJacksonDeserializer());
        addDeserializer(OffsetTime.class, new JavaTimeSerializersV2d0.OffsetTimeJacksonDeserializer());
        addDeserializer(Period.class, new JavaTimeSerializersV2d0.PeriodJacksonDeserializer());
        addDeserializer(Year.class, new JavaTimeSerializersV2d0.YearJacksonDeserializer());
        addDeserializer(YearMonth.class, new JavaTimeSerializersV2d0.YearMonthJacksonDeserializer());
        addDeserializer(ZonedDateTime.class, new JavaTimeSerializersV2d0.ZonedDateTimeJacksonDeserializer());
        addDeserializer(ZoneOffset.class, new JavaTimeSerializersV2d0.ZoneOffsetJacksonDeserializer());
    }

    public static Builder build() {
        return new Builder();
    }

    @Override
    public Map<Class, String> getTypeDefinitions() {
        return TYPE_DEFINITIONS;
    }

    @Override
    public String getTypeNamespace() {
        return GraphSONTokens.GREMLINX_TYPE_NAMESPACE;
    }

    public static final class Builder implements GraphSONModuleBuilder {

        private Builder() {
        }

        @Override
        public GraphSONModule create(final boolean normalize) {
            return new GraphSONXModuleV2d0(normalize);
        }
    }
}
