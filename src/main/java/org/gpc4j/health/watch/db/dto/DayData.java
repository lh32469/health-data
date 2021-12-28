package org.gpc4j.health.watch.db.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.xml.Record;

import java.util.List;

@Data
@Slf4j
public class DayData {

  List<Record> records;

  public float time() {
//    Collections.sort(records);

    return records.stream()
        .map(record -> record.getDuration())
        .reduce((a, b) -> a + b)
        .get();
  }

}
