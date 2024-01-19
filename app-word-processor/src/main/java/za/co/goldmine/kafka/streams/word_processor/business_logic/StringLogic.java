package za.co.goldmine.kafka.streams.word_processor.business_logic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringLogic {

    private static final String WORD_SEPARATOR = " ";

    public static String convertToTitleCaseSplitting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return Arrays
          .stream(text.split(WORD_SEPARATOR))
          .map(word -> word.isEmpty()
            ? word
            : Character.toTitleCase(word.charAt(0)) + word
            .substring(1)
            .toLowerCase())
          .collect(Collectors.joining(WORD_SEPARATOR));
    }

    public static String countWordsAggregator(String text) {

        text = removePunctuation(text.toLowerCase());

        List<String> words = Arrays.asList(
          text.split(" ")
        );

        Map<String, Long> counted = words
          .stream()
          .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counted.toString();
    }

    public static String removePunctuation(String text) {
        return text
          .replaceAll("[^a-zA-Z ]", "");
    }
}
