package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Record")
@Data
public class Record implements Comparable<Record> {

 public final static String SWIMMING = "HKQuantityTypeIdentifierDistanceSwimming";


  public static final DateTimeFormatter DTF
      = DateTimeFormatter.ofPattern("yyyy-MM-dd k:mm:ss Z");

  @XmlAttribute(name = "type")
  private String type;

  @XmlAttribute(name = "creationDate")
  private String creationDate;

  @XmlAttribute(name = "startDate")
  private String startDate;

  @XmlAttribute(name = "endDate")
  private String endDate;

  @XmlAttribute(name = "value")
  private String value;

  @JsonIgnore
  public LocalDateTime getStart() {
    return LocalDateTime.parse(startDate, DTF);
  }

  @JsonIgnore
  public int getDuration() {
    LocalDateTime start = LocalDateTime.parse(startDate, DTF);
    LocalDateTime finish = LocalDateTime.parse(endDate, DTF);

    Duration duration = Duration.between(start, finish);

    return (int) duration.getSeconds();
  }

  @Override
  public int compareTo(Record other) {
    return other.startDate.compareTo(startDate);
  }

}
