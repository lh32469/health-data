package org.gpc4j.health.watch.xml;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

import static org.gpc4j.health.watch.jsf.beans.Constants.SWIMMING_WORKOUT;
import static org.gpc4j.health.watch.jsf.beans.Constants.WALKING_WORKOUT;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Workout")
@Data
@Slf4j
public class Workout {

  private String user;

  @XmlAttribute
  private String workoutActivityType;
  @XmlAttribute
  private Double duration;
  @XmlAttribute
  private String durationUnit;
  @XmlAttribute
  private Double totalDistance;
  @XmlAttribute
  private String totalDistanceUnit;
  @XmlAttribute
  private Double totalEnergyBurned;
  @XmlAttribute
  private String totalEnergyBurnedUnit;
  @XmlAttribute
  private String sourceName;
  @XmlAttribute
  private String sourceVersion;
  @XmlAttribute
  private String device;
  @XmlAttribute
  private String creationDate;
  @XmlAttribute
  private String startDate;
  @XmlAttribute
  private String endDate;

  @XmlElement(name = "WorkoutEvent")
  private List<WorkoutEvent> workoutEvents;
  @XmlElement(name = "MetadataEntry")
  private List<MetadataEntry> metadataEntry;
  @XmlElement(name = "WorkoutStatistics")
  private List<WorkoutStatistics> workoutStatistics;

  public List<WorkoutEvent> getWorkoutEvents() {
    if (null == workoutEvents) {
      log.info("No WorkoutEvents for: {}", this);
      return Collections.emptyList();
    }
    Collections.sort(workoutEvents);
    return workoutEvents;
  }

  public Double getTotalDistance() {

    if (totalDistance == null) {
      switch (workoutActivityType) {

        case SWIMMING_WORKOUT:
          WorkoutStatistics swimming = workoutStatistics.stream()
              .filter(stat ->
                  "HKQuantityTypeIdentifierDistanceSwimming".equals(stat.type))
              .findAny()
              .get();

          if ("YD".equalsIgnoreCase(swimming.unit)) {
            return Math.round(Integer.parseInt(swimming.sum) / 1760.0 * 100) / 100.0;
          }
          break;

        case WALKING_WORKOUT:
          WorkoutStatistics walking = workoutStatistics.stream()
              .filter(stat ->
                  "HKQuantityTypeIdentifierDistanceWalkingRunning".equals(stat.type))
              .findAny()
              .get();

          if ("MI".equalsIgnoreCase(walking.unit)) {
            return Double.parseDouble(walking.sum);
          }
          break;
      }

    }

    return totalDistance;
  }

//  double getTotalDistanceSwimming() {
//    String lapLength = metadataEntry.stream()
//        .filter(entry -> entry.getKey().equals("HKLapLength"))
//        .findAny()
//        .get()
//        .getValue();
//    double value = Double.parseDouble(lapLength.split(" ")[0].trim());
//    String units = lapLength.split(" ")[1].trim();
//
//    // Count total laps
//    long laps = workoutEvents.stream()
//        .filter(event -> event.getType().equals("HKWorkoutEventTypeLap"))
//        .count();
//
//    double conversion = 1.0;
//
//    if ("M".equalsIgnoreCase(units)) {
//      // Convert meters to miles
//      conversion = 0.000621371;
//    }
//
//    double distance = Math.round(value * laps * 100 * conversion) / 100.0;
//    log.info("{} laps @ {} = {} miles", laps, lapLength, distance);
//    return distance;
//  }

  /**
   * Get Duration formatted to min:sec
   */
  public String getDurationF() {
    int minutes = (int) Math.floor(duration);
    int seconds = (int) ((duration - minutes) * 60);
    return String.format("%02d:%2d ", minutes, seconds);
  }

}
