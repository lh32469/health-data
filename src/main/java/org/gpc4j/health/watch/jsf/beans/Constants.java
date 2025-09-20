package org.gpc4j.health.watch.jsf.beans;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Application-wide shared constants.
 */
public interface Constants {

  /**
   * Cookie Keys
   */
  String YEAR_COOKIE_KEY = "YearKey";
  String MONTH_COOKIE_KEY = "MonthKey";
  String DAY_COOKIE_KEY = "DayKey";

  /**
   * Which workoutActivityType to view.
   */
  String WORKOUT_COOKIE_KEY = "WorkoutKey";

  String SWIMMING_WORKOUT = "HKWorkoutActivityTypeSwimming";
  String WALKING_WORKOUT = "HKWorkoutActivityTypeWalking";

  /**
   * Map of human-readable Workout types to workoutActivityTypes/
   */
  Map<String, String> WORKOUT_MAP = Map.of(
      "Swimming", SWIMMING_WORKOUT,
      "Walking", WALKING_WORKOUT
  );

  /**
   * Marker type in WorkoutEvent which separates sets.
   */
  String SEGMENT = "HKWorkoutEventTypeSegment";

  /**
   * Single Lap (pool length) Event.
   */
  String LAP = "HKWorkoutEventTypeLap";

  /**
   * Marker type in WorkoutEvent which indicates workout was paused.
   */
  String PAUSE_WORKOUT = "HKWorkoutEventTypePause";

  /**
   * Marker type in WorkoutEvent which indicates workout was resumed.
   */
  String RESUME_WORKOUT = "HKWorkoutEventTypeResume";

  /**
   * Common Date Format
   */
  DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd k:mm:ss Z");

  /**
   * Swimming Record Type.
   */
  String SWIMMING_RECORD = "HKQuantityTypeIdentifierDistanceSwimming";

  String HEART_RATE = "HKQuantityTypeIdentifierHeartRate";

}
