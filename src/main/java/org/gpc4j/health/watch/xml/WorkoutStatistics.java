package org.gpc4j.health.watch.xml;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WorkoutStatistics")
@Data
@Slf4j
public class WorkoutStatistics {

	@XmlAttribute
	String type;
	@XmlAttribute
	String startDate;
	@XmlAttribute
	String endDate;
	@XmlAttribute
	String average;
	@XmlAttribute
	String minimum;
	@XmlAttribute
	String maximum;
	@XmlAttribute
	String sum;
	@XmlAttribute
	String unit;

}
