package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.gpc4j.health.watch.jsf.beans.Constants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/*
 *
 * <!ELEMENT Record ((MetadataEntry|HeartRateVariabilityMetadataList)*)>
 * <!ATTLIST Record
 *   type          CDATA #REQUIRED
 *   unit          CDATA #IMPLIED
 *   value         CDATA #IMPLIED
 *   sourceName    CDATA #REQUIRED
 *   sourceVersion CDATA #IMPLIED
 *   device        CDATA #IMPLIED
 *   creationDate  CDATA #IMPLIED
 *   startDate     CDATA #REQUIRED
 *   endDate       CDATA #REQUIRED
 * >
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Record")
@Data
public class Record implements Comparable<Record>, Constants {

  public String getId() {
    return UUID.nameUUIDFromBytes(toString().getBytes()).toString();
  }

  private String user;

  @XmlAttribute
  private String type;

  @XmlAttribute
  private String unit;

  @XmlAttribute
  private String value;

  @XmlAttribute
  private String sourceName;

  @XmlAttribute
  private String sourceVersion;

  @XmlAttribute
  private String creationDate;

  @XmlAttribute
  private String startDate;

  @XmlAttribute
  private String endDate;

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
