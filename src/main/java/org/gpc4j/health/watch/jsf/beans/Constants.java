package org.gpc4j.health.watch.jsf.beans;

import java.time.format.DateTimeFormatter;

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

  String SWIMMING_WORKOUT = "HKWorkoutActivityTypeSwimming";

  /**
   * Marker type in WorkoutEvent which separates sets.
   */
  String SEGMENT = "HKWorkoutEventTypeSegment";

  /**
   * Common Date Format
   */
  DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd k:mm:ss Z");

}
