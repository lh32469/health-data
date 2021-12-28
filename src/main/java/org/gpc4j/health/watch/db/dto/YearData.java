package org.gpc4j.health.watch.db.dto;

import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.xml.Record;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.gpc4j.health.watch.xml.Record.SWIMMING;

@Slf4j
public class YearData {

  private final int year;

  private final List<Record> records;

  public YearData(int year, List<Record> records) {
    this.year = year;
    this.records = records;
  }

  public int getYear() {
    return year;
  }

  /**
   * Get the MonthData in this year.
   *
   * @param month Jan -> 1, Feb -> 2, etc..
   */
  public MonthData get(int month) {

    String prefix = String.format("%d-%02d", year, month);

    List<Record> monthRecords = records.stream()
        .filter(record -> record.getType().startsWith(SWIMMING))
        .filter(record -> record.getStartDate().startsWith(prefix))
        .collect(Collectors.toList());

    log.debug("{} = {}", prefix, monthRecords.size());

    return new MonthData(monthRecords);
  }

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

  public static void main(String[] args) {
    YearData yd = new YearData(2021, Collections.emptyList());
    yd.get(1);
  }

}
