package org.gpc4j.health.watch.xml;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "HealthData")
@Data
public class HealthData {

  @XmlElement(name = "Record")
  private List<Record> records;

  @XmlElement(name = "Workout")
  private List<Workout> workouts;

}
