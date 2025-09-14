package org.gpc4j.health.watch.db.docs;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class HeartRateReading {

  private String id;
  private String user;
  private float value;
  private ZonedDateTime date;

  public String getId() {
    return "HRR/" + user + "." + date.toEpochSecond();
  }



}
