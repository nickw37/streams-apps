package za.co.goldmine.kafka.streams.fitness_duplicate_detector;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import za.co.goldmine.kafka.streams.fitness_duplicate_detector.topology.FitnessProcessorTopology;

@SpringBootApplication
public class FitnessDuplicateDetectorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FitnessDuplicateDetectorApplication.class);
        app.run();
    }

    @Override
    public void run(String... args) throws Exception {
        FitnessProcessorTopology fitnessProcessorTopology = new FitnessProcessorTopology();
        fitnessProcessorTopology.startStreamsApp();
    }
}
