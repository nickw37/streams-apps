package za.co.goldmine.kafka.streams.word_processor.topology;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.stereotype.Component;
import za.co.goldmine.kafka.streams.word_processor.streams_builder.WordProcessorBuilder;

import java.util.Properties;

@Component
public class WordProcessorTopology {

    private final String topicRawData = "inbound-raw-data";
    private final String topicTitleCase = "outbound-title-case";
    private final String topicWordCounts = "outbound-word-counts";
    private final String topicEvenLengthWords = "outbound-even-length-words";
    private final String topicOddLengthWords = "outbound-odd-length-words";

    public void startStreamsApp() {
        Topology topology = createTopology();
        KafkaStreams streams = new KafkaStreams(topology, getStreamsProperties());
        streams.start();
    }

    private Topology createTopology() {
        // Create streams builder
        StreamsBuilder builder = new WordProcessorBuilder()
          .setupWordCountFlow(topicRawData, topicWordCounts)
          .setupWordLengthFlow(topicRawData, topicEvenLengthWords, topicOddLengthWords)
          .setupTitleCaseFlow(topicRawData, topicTitleCase)
          .getBuilder();

        return builder.build();
    }

    private Properties getStreamsProperties() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "kafka-broker1:9092");
        properties.put("application.id", "word-processor-streams-app");
        properties.put("client.id", "word-processor-streams-client");
        properties.put("auto.offset.reset", "earliest");
        properties.put("commit.interval.ms", "100000");
        properties.put(AdminClientConfig.METRICS_RECORDING_LEVEL_CONFIG, "DEBUG");

        return properties;
    }
}
