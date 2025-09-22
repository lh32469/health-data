package org.gpc4j.health.watch.xml;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.gpc4j.health.watch.jsf.beans.Constants.DTF;
import static org.gpc4j.health.watch.jsf.beans.Constants.HEART_RATE;
import static org.gpc4j.health.watch.jsf.beans.Constants.LAP;
import static org.gpc4j.health.watch.jsf.beans.Constants.SWIMMING_WORKOUT;

@Slf4j
public class WorkoutDataProcessor {

  static final ForkJoinPool POOL = new ForkJoinPool(50);

  public static HealthData processData(HealthData data) {
    return POOL.submit(() -> _processData(data)).join();
  }

  private static HealthData _processData(HealthData data) {

    long start = System.currentTimeMillis();

    log.info("Loaded {} Records", data.getRecords().size());
    log.info("Loaded {} Workouts", data.getWorkouts().size());

    Map<LocalDate, List<Record>>
        recordsByDate = getHeartRateRecordsByDate(data);

    log.info("Loaded {} Heart Rate Record Days", recordsByDate.size());

    data.getWorkouts()
        .parallelStream()
        .filter(workout ->
                    workout.getWorkoutActivityType().equals(SWIMMING_WORKOUT))
        .forEach(workout -> {
//          log.info("Processing Workout starting: {}", workout.getStart().toLocalDate());

          // Get heart rates for duration of workout
          List<Record> heartRatesForWorkout =
              recordsByDate.get(workout.getStart().toLocalDate())
                           .stream()
                           .filter(record ->
                                       record.getStart().isAfter(workout.getStart()))
                           .filter(record ->
                                       record.getEnd().isBefore(workout.getEnd()))
                           .collect(Collectors.toList());

//          log.info("Found {} Heart Rate Records for Workout day {}",
//                   heartRatesForWorkout.size(), date);

          workout.getWorkoutEvents()
                 .stream()
                 .filter(event -> event.getType().equals(LAP))
                 .forEach(event -> {
                   LocalDateTime time = LocalDateTime.parse(event.getDate(), DTF);
                   Optional<Record> heartRateRecord =
                       findHeartRate(heartRatesForWorkout, time);

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

  static Map<LocalDate, List<Record>> getHeartRateRecordsByDate(HealthData data) {
    Map<LocalDate, List<Record>> recordsByDate =
        data.getRecords().stream()
            .filter(record -> record.getType().equals(HEART_RATE))
            .collect(Collectors.groupingBy(record -> record.getStart().toLocalDate()));
    return recordsByDate;
  }

  static Optional<Record> findHeartRate(List<Record> heartRateData, LocalDateTime start) {

//    log.debug("heartRateData.size() = {}", heartRateData.size());

    // Only consider records after the start time
    // and within 90 seconds of start time

    List<Record> after =
        heartRateData.stream()
                     .filter(record ->
                                 record.getStart().isAfter(start))
                     .filter(record ->
                                 record.getEnd()
                                       .isBefore(start.plusSeconds(65)))
                     .collect(Collectors.toList());

//    log.debug("after.size() = {}", after.size());

    // Expanding range, in seconds, to find a match
    final AtomicInteger range = new AtomicInteger(0);

    // Check for match within 60 seconds
    while (range.getAndIncrement() < 60) {

      Optional<Record> result =
          after.stream()
               .filter(record ->
                           record.getEnd()
                                 .isBefore(start.plusSeconds(range.get())))
               .findFirst();

      if (result.isPresent()) {
        return result;
      }

    }

    return Optional.empty();
  }

}
