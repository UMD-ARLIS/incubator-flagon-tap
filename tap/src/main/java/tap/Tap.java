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
package tap.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.TimeWindows;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.time.Duration;

public class Tap {

    private static KafkaJsonSchemaSerde<Log> logSerde(Properties props) {
        final KafkaJsonSchemaSerde<Log> jsonSchemaSerde = new KafkaJsonSchemaSerde<>();
        Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG, props.getProperty("schema.registry.url"));
        jsonSchemaSerde.configure(serdeConfig, false);
        return jsonSchemaSerde;
    }

    protected Topology buildTopology(Properties props, final KafkaJsonSchemaSerde<Log> jsonSchemaSerde) {
        final StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String, JsonNode> rawStream =
            builder.stream("raw-logs")
            .peek((key, value) -> System.out.println("Key: " + key + " Value: " + value));

        rawStream.groupByKey()
                 .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(30),  Duration.ofMinutes(1)))
                //  .peek((key, value) -> System.out.println("Key: " + key + " Value: " + value));
    
        return builder.build();
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put("schema.registry.url", "http://localhost:8081");

        Topology topology = this.buildTopology(props, this.logSerde(props));
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
