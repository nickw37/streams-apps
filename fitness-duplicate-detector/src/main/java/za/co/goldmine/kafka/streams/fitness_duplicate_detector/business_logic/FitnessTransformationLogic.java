package za.co.goldmine.kafka.streams.fitness_duplicate_detector.business_logic;

import za.co.goldmine.kafka.streams.fitness_duplicate_detector.avro.FitnessEvent;

public class FitnessTransformationLogic {
    public static String generateHash(FitnessEvent fitnessEvent) {

        return "" + fitnessEvent.getStartTimeInSeconds()
          + fitnessEvent.getDurationInSeconds()
          + fitnessEvent.getActiveKilocalories()
          + fitnessEvent.getSteps();
    }
}
