package org.gpc4j.health.watch.db.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.xml.Record;

import java.util.List;

@Data
@Slf4j
public class MonthData {

  public MonthData(List<Record> records) {
    this.records = records;
  }

  List<Record> records;

  /**
   * Get total distance in miles.
   */
  public float getDistance() {
    int yards = records.stream()
        .map(record -> Integer.parseInt(record.getValue()))
        .reduce((a, b) -> a + b)
        .orElse(0);

    return (float) (yards * 3 / 5024.0);
  }

}
