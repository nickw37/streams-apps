package za.co.goldmine.kafka.streams.word_processor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import za.co.goldmine.kafka.streams.word_processor.topology.WordProcessorTopology;

@SpringBootApplication
public class AppWordProcessorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AppWordProcessorApplication.class);
        app.run();
    }

    @Override
    public void run(String... args) throws Exception {
        WordProcessorTopology wordProcessorTopology = new WordProcessorTopology();
        wordProcessorTopology.startStreamsApp();
    }
}
