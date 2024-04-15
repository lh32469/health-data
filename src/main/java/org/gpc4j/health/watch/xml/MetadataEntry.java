package org.gpc4j.health.watch.xml;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MetadataEntry")
@Data
@Slf4j
public class MetadataEntry {

	@XmlAttribute
	private String key;
	@XmlAttribute
	private String value;

}
