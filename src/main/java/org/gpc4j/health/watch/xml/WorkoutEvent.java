package org.gpc4j.health.watch.xml;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WorkoutEvent")
@Data
public class WorkoutEvent {

  @XmlAttribute
  private String type;

  @XmlAttribute
  private String date;

  @XmlAttribute
  private String duration;

  @XmlAttribute
  private String durationUnit;

}
