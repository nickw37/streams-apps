package za.co.goldmine.kafka.streams.word_processor.streams_builder;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static za.co.goldmine.kafka.streams.word_processor.business_logic.StringLogic.*;

public class WordProcessorBuilder {

    private final StreamsBuilder builder = new StreamsBuilder();

    public WordProcessorBuilder setupTitleCaseFlow(String inTopic, String outTopic) {

        builder
          .stream(inTopic, Consumed.with(Serdes.String(), Serdes.String()))
          .map((String key, String value) -> {
              String transformedValue = convertToTitleCaseSplitting(value);
              return KeyValue.pair(key, transformedValue);
          }) //"hello my name is Bhavesh" -> "Hello My Name Is Bhavesh"
          .to(outTopic, Produced.with(Serdes.String(), Serdes.String()));
        return this;
    }

    public WordProcessorBuilder setupWordCountFlow(String inTopic, String outTopic) {

        builder
          .stream(inTopic, Consumed.with(Serdes.String(), Serdes.String()))
          .map((String key, String value) -> {
              String transformedValue = countWordsAggregator(value);
              return KeyValue.pair(key, transformedValue);
          })
          .to(outTopic, Produced.with(Serdes.String(), Serdes.String()));
        return this;
    }

    public WordProcessorBuilder setupWordLengthFlow(String inTopic, String evenTopic, String oddTopic) {

        Map<String, KStream<String, String>> branches = builder
          .stream(inTopic, Consumed.with(Serdes.String(), Serdes.String()))
          .map((String key, String sentence) -> KeyValue.pair(key, removePunctuation(sentence)))
          // Daniel
          .flatMap((String key, String sentence) ->
            Arrays.stream(sentence.split(" "))
              .map(word -> KeyValue.pair(key, word))
              .collect(Collectors.toSet()))
          // The cooler Daniel
          .split(Named.as("word-length-"))
          .branch((String key, String value) -> value.length() % 2 == 0, Branched.as("even"))
          .branch((String key, String value) -> value.length() % 2 != 0, Branched.as("odd"))
          .defaultBranch(Branched.as("NaN"));

        branches.get("word-length-even")
          .to(evenTopic, Produced.with(Serdes.String(), Serdes.String()));

        branches.get("word-length-odd")
          .to(oddTopic, Produced.with(Serdes.String(), Serdes.String()));

        return this;
    }

    public StreamsBuilder getBuilder() {
        return this.builder;
    }
}
