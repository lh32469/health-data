package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.gpc4j.health.watch.jsf.beans.Constants.LAP;
import static org.gpc4j.health.watch.jsf.beans.Constants.SEGMENT;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WorkoutEvent")
@Data
@Slf4j
public class WorkoutEvent implements Comparable<WorkoutEvent> {

  static final DateTimeFormatter DTF =
      DateTimeFormatter.ofPattern("yyyy-MM-dd k:mm:ss Z");

  @XmlAttribute
  private String type;

  @XmlAttribute
  private String date;

  // TODO: refactor to double.
  @XmlAttribute
  private String duration;

  @XmlAttribute
  private String durationUnit;

  @JsonIgnore
  public String getDurationF() {
    double _duration = Double.parseDouble(duration);
    int minutes = (int) Math.floor(_duration);
    int seconds = (int) ((_duration - minutes) * 60);
    return String.format("%02d:%02d ", minutes, seconds);
  }

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
