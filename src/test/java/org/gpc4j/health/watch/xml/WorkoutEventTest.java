package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Slf4j
public class WorkoutEventTest {

  @Test
  public void workoutEventSorting() throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();

    String segmentString = "{\n" +
        "            \"type\": \"HKWorkoutEventTypeSegment\",\n" +
        "            \"date\": \"2022-09-16 15:27:45 -0700\",\n" +
        "            \"duration\": \"32.43460502028465\",\n" +
        "            \"durationUnit\": \"min\"\n" +
        "        }";

    String lapString = "        {\n" +
        "            \"type\": \"HKWorkoutEventTypeLap\",\n" +
        "            \"date\": \"2022-09-16 15:27:45 -0700\",\n" +
        "            \"duration\": \"0.3743648151556651\",\n" +
        "            \"durationUnit\": \"min\"\n" +
        "        }";

    WorkoutEvent segment = mapper.readValue(segmentString, WorkoutEvent.class);
    WorkoutEvent lap = mapper.readValue(lapString, WorkoutEvent.class);
    log.info(segment.toString());
    log.info(lap.toString());

    // Laps at same time as Segment get sorted after Segment
    assertThat(lap.compareTo(segment), is(1));

    // Laps at same time as Segment get sorted after Segment
    assertThat(segment.compareTo(lap), is(-1));

    List<WorkoutEvent> events = new LinkedList<>();
    events.add(lap);
    events.add(segment);

    log.info("events = {}", events);
    Collections.sort(events);
    log.info("events = {}", events);

    assertThat(events.get(0), is(segment));
    assertThat(events.get(1), is(lap));

  }

}
