/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tap;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.KeyValue;

import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Suppressed;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Tap {

    /**
     * A serde for any class that implements {@link JSONSerdeCompatible}. Note that the classes also need to
     * be registered in the {@code @JsonSubTypes} annotation on {@link JSONSerdeCompatible}.
     *
     * @param <T> The concrete type of the class that gets de/serialized
     */
    public static class JSONSerde<T extends JSONSerdeCompatible> implements Serializer<T>, Deserializer<T>, Serde<T> {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        @Override
        public void configure(final Map<String, ?> configs, final boolean isKey) {}

        @SuppressWarnings("unchecked")
        @Override
        public T deserialize(final String topic, final byte[] data) {
            if (data == null) {
                return null;
            }

            try {
                return (T) OBJECT_MAPPER.readValue(data, JSONSerdeCompatible.class);
            } catch (final IOException e) {
                throw new SerializationException(e);
            }
        }

        @Override
        public byte[] serialize(final String topic, final T data) {
            if (data == null) {
                return null;
            }

            try {
                return OBJECT_MAPPER.writeValueAsBytes(data);
            } catch (final Exception e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        }

        @Override
        public void close() {}

        @Override
        public Serializer<T> serializer() {
            return this;
        }

        @Override
        public Deserializer<T> deserializer() {
            return this;
        }
    }

    /**
     * An interface for registering types that can be de/serialized with {@link JSONSerde}.
     */
    @SuppressWarnings("DefaultAnnotationParam") // being explicit for the example
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "logType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = RawLog.class, name = "raw"),
    })
    public interface JSONSerdeCompatible {}

    static public class RawLog implements JSONSerdeCompatible {
        public String target;
        public String[] path;
        public String pageUrl;
        public String pageTitle;
        public String pageReferrer;
        public Map<String, String> browser;
        public Long clientTime;
        public Double microTime;
        public Map<String, Integer> location;
        public Map<String, Integer> scrnRes;
        public String type;
        public String userAction;
        public Map<String, Object> details;
        public String userId;
        public String toolVersion;
        public String toolName;
        public String useraleVersion;
        public String sessionID;
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, JsonTimestampExtractor.class);

        final StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String, JSONSerdeCompatible> rawStream =
            builder.stream("raw-logs", Consumed.with(Serdes.String(), new JSONSerde<>()))
                .peek((key, value) -> System.out.println("session: " + key + " log: " + value));
        
        rawStream.groupByKey()
            .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(10),  Duration.ofSeconds(5)))
            .count()
            .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
            .toStream()
            .map((wk, value) -> KeyValue.pair(wk.key(),value))
            .foreach((key, value) -> System.out.println("session: " + key + " count: " + value));
        
        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
