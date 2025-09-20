package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.gpc4j.health.watch.jsf.beans.Constants.DTF;
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

  @JsonIgnore
  public LocalDateTime getStart() {
    return LocalDateTime.parse(startDate, DTF);
  }

  @JsonIgnore
  public LocalDateTime getEnd() {
    return LocalDateTime.parse(endDate, DTF);
  }

  public Double getTotalDistance() {

    if (totalDistance == null) {
      switch (workoutActivityType) {

        case SWIMMING_WORKOUT:
          Optional<WorkoutStatistics> swimming =
              getStatistic("HKQuantityTypeIdentifierDistanceSwimming");

          if (swimming.isPresent() && "YD".equalsIgnoreCase(swimming.get().unit)) {
            return Math.round(Integer.parseInt(swimming.get().sum) / 1760.0 * 100) / 100.0;
          } else {
            return 0.0;
          }

        case WALKING_WORKOUT:
          Optional<WorkoutStatistics> walking =
              getStatistic("HKQuantityTypeIdentifierDistanceWalkingRunning");

          if (walking.isPresent() && "MI".equalsIgnoreCase(walking.get().unit)) {
            return Double.parseDouble(walking.get().sum);
          } else {
            return 0.0;
          }
      }

    }

    return totalDistance;
  }

  public Double getTotalEnergyBurned() {

    if (totalEnergyBurned == null) {

      try {

        Optional<WorkoutStatistics> base =
            getStatistic("HKQuantityTypeIdentifierBasalEnergyBurned");

        Optional<WorkoutStatistics> activity =
            getStatistic("HKQuantityTypeIdentifierActiveEnergyBurned");

        if (base.isEmpty() || activity.isEmpty()) {
          return 0.0;
        }

        totalEnergyBurned = Double.parseDouble(base.get().sum)
            + Double.parseDouble(activity.get().sum);

      } catch (NullPointerException ex) {
        log.info("ex = {}", ex);
      }
    }

    return totalEnergyBurned;
  }

  Optional<WorkoutStatistics> getStatistic(final String statisticName) {
    Optional<WorkoutStatistics> statistics = workoutStatistics.stream()
                                                              .filter(stat -> statisticName.equals(
                                                                  stat.type))
                                                              .findAny();

    if (statistics.isEmpty()) {
      log.error("No " + statisticName + " for " + workoutActivityType +
                    "; " + startDate);
    }
    return statistics;
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
