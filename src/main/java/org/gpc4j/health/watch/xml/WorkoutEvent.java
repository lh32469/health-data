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

  // TODO: refactor to double.
  @XmlAttribute
  private String duration;

  @XmlAttribute
  private String durationUnit;

  public String getDurationF() {
    double _duration = Double.parseDouble(duration);
    int minutes = (int) Math.floor(_duration);
    int seconds = (int) ((_duration - minutes) * 60);
    return String.format("%02d:%2d ", minutes, seconds);
  }

}
