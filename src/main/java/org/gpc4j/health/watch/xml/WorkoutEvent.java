package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

import static org.gpc4j.health.watch.jsf.beans.Constants.DTF;
import static org.gpc4j.health.watch.jsf.beans.Constants.LAP;
import static org.gpc4j.health.watch.jsf.beans.Constants.SEGMENT;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WorkoutEvent")
@Data
@Slf4j
public class WorkoutEvent implements Comparable<WorkoutEvent> {

  @XmlAttribute
  private String type;

  @XmlAttribute
  private String date;

  @XmlAttribute
//  @JsonSerialize(using = CustomDoubleSerializer.class)
  private Double duration;

  @XmlAttribute
  private String durationUnit;

  @JsonIgnore
  public String getDurationF() {
    int minutes = (int) Math.floor(duration);
    int seconds = (int) ((duration - minutes) * 60);
    return String.format("%02d:%02d ", minutes, seconds);
  }

  private short heartRate;

  @Override
  public int compareTo(WorkoutEvent other) {

    LocalDateTime otherTime = LocalDateTime.parse(other.date, DTF);
    LocalDateTime thisTime = LocalDateTime.parse(this.date, DTF);

    if (thisTime.isBefore(otherTime)) {
      return -1;
    } else if (thisTime.isAfter(otherTime)) {
      return 1;
    } else if (other.type.equals(SEGMENT) && this.type.equals(LAP)) {
      return 1;
    } else if (other.type.equals(LAP) && this.type.equals(SEGMENT)) {
      return -1;
    } else {
      return 0;
    }

  }

}
