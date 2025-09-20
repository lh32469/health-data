package org.gpc4j.health.watch.xml;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static org.gpc4j.health.watch.jsf.beans.Constants.DTF;
import static org.gpc4j.health.watch.jsf.beans.Constants.HEART_RATE;
import static org.gpc4j.health.watch.jsf.beans.Constants.LAP;
import static org.gpc4j.health.watch.jsf.beans.Constants.SWIMMING_WORKOUT;

@Slf4j
public class WorkoutDataProcessor {

  static final ForkJoinPool POOL = new ForkJoinPool(25);

  public static HealthData processData(HealthData data) {
    return POOL.submit(() -> _processData(data)).join();
  }

  private static HealthData _processData(HealthData data) {

    long start = System.currentTimeMillis();

    log.info("Loaded {} Records", data.getRecords().size());
    log.info("Loaded {} Workouts", data.getWorkouts().size());

    List<Record> heartrates =
        data.getRecords().parallelStream()
            .filter(record -> record.getType().equals(HEART_RATE))
            .sorted(Comparator.comparing(Record::getStart))
            .collect(Collectors.toList());

    log.info("Filtered {} Heart Rate Records", heartrates.size());

    data.getWorkouts()
        .parallelStream()
        .filter(workout ->
                    workout.getWorkoutActivityType().equals(SWIMMING_WORKOUT))
        .forEach(workout -> {
          workout.setUser("JUnit");
          log.debug("Processing Workout starting: {}", workout.getStart());

          // Get heart rates for duration of workout
          List<Record> heartRatesForWorkout =
              heartrates.stream()
                        .filter(record ->
                                    record.getStart()
                                          .isAfter(
                                              workout.getStart()))
                        .filter(record ->
                                    record.getEnd()
                                          .isBefore(workout.getEnd()))
                        .sorted(Comparator.comparing(Record::getStart))
                        .collect(Collectors.toList());

          workout.getWorkoutEvents()
                 .stream()
                 .filter(event -> event.getType().equals(LAP))
                 .forEach(event -> {
                   LocalDateTime date = LocalDateTime.parse(event.getDate(), DTF);
                   Optional<Record> heartRateRecord =
                       findHeartRate(heartRatesForWorkout, date);

                   if (heartRateRecord.isPresent()) {
//                     log.info("Found heart rate for workout = " + event);

                     Record record = heartRateRecord.get();
                     float value = Float.parseFloat(record.getValue());
                     short rate = (short) value;
                     event.setHeartRate(rate);
                   } else {
//                     log.warn("Could not find heart rate for workout = " + event);
                   }

                 });

        });

    log.info("Processed {} Workouts in {} ms",
             data.getWorkouts().size(),
             System.currentTimeMillis() - start);

    return data;
  }

  static Optional<Record> findHeartRate(List<Record> heartrates, LocalDateTime start) {

    Optional<Record> result = heartrates.stream()
                                        .filter(record ->
                                                    record.getStart().isAfter(start))
                                        .filter(record ->
                                                    record.getEnd()
                                                          .isBefore(start.plusMinutes(1)))
                                        .findFirst();

//    result.ifPresent(heartrates::remove);

    return result;
  }

}
