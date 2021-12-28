package org.gpc4j.health.watch.xml;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Workout")
@Data
public class Workout {

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

}
