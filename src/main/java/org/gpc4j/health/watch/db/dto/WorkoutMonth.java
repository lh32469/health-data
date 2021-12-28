package org.gpc4j.health.watch.db.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.xml.Workout;

import java.text.DateFormatSymbols;
import java.util.List;

@Data
@Slf4j
public class WorkoutMonth {

  /**
   * @ month Jan -> 1, Feb -> 2, etc..
   */
  private final int month;

  List<Workout> workouts;

  public WorkoutMonth(int month, List<Workout> workouts) {
    this.month = month;
    this.workouts = workouts;
  }

  public String getName() {
    return new DateFormatSymbols().getMonths()[month - 1];
  }

  /**
   * Get total distance in miles.
   */
  public double getDistance() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getTotalDistance)
        .sum();
    log.debug("month/distance = {}/{}", month, distance);
    return distance;
  }

  /**
   * Get total calories burned.
   */
  public double getCalories() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getTotalEnergyBurned)
        .sum();
    log.debug("month/calories = {}/{}", month, distance);
    return distance;
  }



  /**
   * Get total hours spent working out.
   */
  public double getHours() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getDuration)
        .sum() / 60;
    log.debug("month/hours = {}/{}", month, distance);
    return distance;
  }




  /**
   * Get total number of workouts.
   */
  public double getNumWorkouts() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getDuration)
        .sum() / 60;
    log.debug("month/hours = {}/{}", month, distance);
    return distance;
  }

}
