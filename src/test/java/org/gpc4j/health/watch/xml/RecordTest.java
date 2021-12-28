package org.gpc4j.health.watch.xml;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.LoggerFactory.getLogger;

class RecordTest {

  private static final Logger log
      = getLogger(RecordTest.class);


  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }


  @Test
  public void calculateDuration() {

    Record r1 = new Record();
    r1.setStartDate("2021-11-02 13:46:45 -0800");
    r1.setEndDate("2021-11-02 13:47:00 -0800");

    LocalDateTime start =r1.getStart();

    log.info("start = {}", start);

    log.info("r1.getDuration() = {}", r1.getDuration());

    assertEquals(15,r1.getDuration());
  }

}
