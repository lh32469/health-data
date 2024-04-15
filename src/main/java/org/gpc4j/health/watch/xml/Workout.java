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

  public List<WorkoutEvent> getWorkoutEvents() {
    if (null == workoutEvents) {
      log.info("No WorkoutEvents for: {}", this);
      return Collections.emptyList();
    }
    Collections.sort(workoutEvents);
    return workoutEvents;
  }

  /**
   * Get Duration formatted to min:sec
   */
  public String getDurationF() {
    int minutes = (int) Math.floor(duration);
    int seconds = (int) ((duration - minutes) * 60);
    return String.format("%02d:%2d ", minutes, seconds);
  }

}
