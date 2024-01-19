package za.co.goldmine.kafka.streams.fitness_duplicate_detector.streams_builder;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import za.co.goldmine.kafka.streams.fitness_duplicate_detector.avro.FitnessDuplicateEvent;
import za.co.goldmine.kafka.streams.fitness_duplicate_detector.avro.FitnessEvent;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static za.co.goldmine.kafka.streams.fitness_duplicate_detector.business_logic.FitnessTransformationLogic.generateHash;


public class FitnessFlowsBuilder {

    // Builder pattern

    private final StreamsBuilder builder = new StreamsBuilder();
    private final Map<String, String> schemaRegistryMap;

    public FitnessFlowsBuilder(String schemaRegistryUrl) {
        this.schemaRegistryMap = Collections.singletonMap("schema.registry.url", schemaRegistryUrl);
    }

    public FitnessFlowsBuilder setupEventDuplicateDetectorFlow(String inTopic, String outTopic) {
        Serde<FitnessEvent> fitnessAvroSerde = new SpecificAvroSerde<>();
        fitnessAvroSerde.configure(schemaRegistryMap, false);

        Serde<FitnessDuplicateEvent> fitnessDuplicateEventSerde = new SpecificAvroSerde<>();
        fitnessDuplicateEventSerde.configure(schemaRegistryMap, false);

        KTable<String, FitnessEvent> hashEvent = builder
          .stream(inTopic, Consumed.with(Serdes.String(), fitnessAvroSerde))
          .toTable();

        KStream<String, Long> hashCounts = builder
          .stream(inTopic, Consumed.with(Serdes.String(), fitnessAvroSerde))
          .groupByKey()
          .count()
          .filter((key, value) -> value > 1)
          .toStream()
          .filter((key, value) -> value != null);

        hashCounts
          .join(hashEvent, new HashCountJoiner())
          .to(outTopic, Produced.with(Serdes.String(), fitnessDuplicateEventSerde));

        return this;
    }

    public FitnessFlowsBuilder setupHashGeneratorFlow(String inTopic, String outTopic) {

        Serde<FitnessEvent> fitnessAvroSerde = new SpecificAvroSerde<>();
        fitnessAvroSerde.configure(schemaRegistryMap, false);

        builder
          .stream(inTopic, Consumed.with(Serdes.String(), fitnessAvroSerde))
          .map((k, v) -> KeyValue.pair(generateHash(v), v))
          .to(outTopic, Produced.with(Serdes.String(), fitnessAvroSerde));

        return this;
    }

    public FitnessFlowsBuilder setupDuplicateGeneratorFlow(String topic) {
        final int[] messageCount = {0};

        Serde<FitnessEvent> fitnessAvroSerde = new SpecificAvroSerde<>();
        fitnessAvroSerde.configure(schemaRegistryMap, false);

        builder
          .stream(topic, Consumed.with(Serdes.String(), fitnessAvroSerde))
          .map((key, value) -> {
              messageCount[0]++;
              return KeyValue.pair(key, value);
          })
          .filter((key, value) -> messageCount[0] % 10 == 0)
          .map((key, value) -> KeyValue.pair("duplicate", value))
          .to(topic, Produced.with(Serdes.String(), fitnessAvroSerde));

        return this;
    }

    public StreamsBuilder getBuilder() {
        return this.builder;
    }

    /*---------------------------------------------------------------------------------------------------*/
    // Private helper classes/functions
    /*---------------------------------------------------------------------------------------------------*/
    public static class HashCountJoiner implements ValueJoiner<Long, FitnessEvent, FitnessDuplicateEvent> {

        public FitnessDuplicateEvent apply(Long hashCount, FitnessEvent fitnessHashEvent) {
            return FitnessDuplicateEvent.newBuilder()
              .setCOUNTDUPLICATES(hashCount)
              .setUSERID(fitnessHashEvent.getUserid())
              .setTENANTID(fitnessHashEvent.getTenantId())
              .build();
        }
    }
}
