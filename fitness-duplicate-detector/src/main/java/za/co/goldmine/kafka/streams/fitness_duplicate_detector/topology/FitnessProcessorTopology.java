package za.co.goldmine.kafka.streams.fitness_duplicate_detector.topology;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.stereotype.Component;
import za.co.goldmine.kafka.streams.fitness_duplicate_detector.streams_builder.FitnessFlowsBuilder;

import java.util.Properties;

@Component
public class FitnessProcessorTopology {

    private final String schemaRegistryUrl = "http://schema-registry:8081";

    public void startStreamsApp() {
        Topology topology = createTopology();
        KafkaStreams streams = new KafkaStreams(topology, getStreamsProperties());
        streams.start();
    }

    private Topology createTopology() {
        StreamsBuilder builder = new FitnessFlowsBuilder(this.schemaRegistryUrl)
          .setupDuplicateGeneratorFlow("fitness-event")
          .setupHashGeneratorFlow("fitness-event", "fitness-hash-event")
          .setupEventDuplicateDetectorFlow("fitness-hash-event", "fitness-duplicate-event")
          .getBuilder();

        return builder.build();
    }

    private Properties getStreamsProperties() {
        Properties properties = new Properties();

        // Normal
        properties.put("bootstrap.servers", "kafka-broker1:9092");
        properties.put("application.id", "fitness-duplicate-detector-app");
        properties.put("client.id", "fitness-duplicate-detector-client");
        properties.put("auto.offset.reset", "earliest");
        properties.put("commit.interval.ms", "100000");

        // Schema Registry
        properties.put("schema.registry.url", this.schemaRegistryUrl);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);

        return properties;
    }
}
