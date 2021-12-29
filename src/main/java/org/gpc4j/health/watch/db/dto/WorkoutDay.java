package org.gpc4j.health.watch.db.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.xml.Workout;

import java.util.List;

@Data
@Slf4j
public class WorkoutDay {

  /**
   * Day of the Month.
   */
  private final int day;

  List<Workout> workouts;

  public WorkoutDay(int day, List<Workout> workouts) {
    this.day = day;
    this.workouts = workouts;
  }

  /**
   * Get total distance in miles.
   */
  public double getDistance() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getTotalDistance)
        .sum();
    log.debug("month/distance = {}/{}", day, distance);
    return distance;
  }

  /**
   * Get total calories burned.
   */
  public double getCalories() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getTotalEnergyBurned)
        .sum();
    log.debug("month/calories = {}/{}", day, distance);
    return distance;
  }

  /**
   * Get total hours spent working out.
   */
  public double getHours() {
    return getMinutes() / 60.0;
  }

  /**
   * Get total hours spent working out.
   */
  public long getMinutes() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getDuration)
        .sum();
    log.debug("month/minutes = {}/{}", day, distance);
    return Math.round(distance);
  }

}
