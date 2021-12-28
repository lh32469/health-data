package org.gpc4j.health.watch.db.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.xml.Workout;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class WorkoutYear {

  /**
   * Get Year from Session passed in via redirect from workouts.xhtml page.
   */
  private final int year;

  List<Workout> workouts;
  List<WorkoutMonth> months;


  public WorkoutYear(int year, List<Workout> workouts) {
    this.year = year;
    this.workouts = workouts;
    log.info("year/workouts = {}/{}", year, workouts.size());
  }

  public int getYear() {
    return year;
  }

  /**
   * Get the MonthData in this year.
   *
   * @param month Jan -> 1, Feb -> 2, etc..
   */
  public WorkoutMonth get(int month) {

    String prefix = String.format("%d-%02d", year, month);
    log.debug("prefix = {}", prefix);

    List<Workout> _workouts = workouts.stream()
        .filter(workout -> workout.getStartDate().startsWith(prefix))
        .collect(Collectors.toList());

    WorkoutMonth workoutMonth = new WorkoutMonth(month, _workouts);
    return workoutMonth;
  }

  /**
   * Get total distance in miles.
   */
  public double getDistance() {
    double distance = workouts.stream()
        .mapToDouble(Workout::getTotalDistance)
        .sum();
    log.info("year/distance = {}/{}", year, distance);
    return distance;
  }

}
