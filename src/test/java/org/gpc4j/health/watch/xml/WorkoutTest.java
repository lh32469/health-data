package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.LinkedList;

import static org.gpc4j.health.watch.jsf.beans.Constants.DTF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Slf4j
public class WorkoutTest {

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

    Workout workout = new Workout();
    workout.setWorkoutEvents(new LinkedList<>());
    workout.getWorkoutEvents().add(lap);
    workout.getWorkoutEvents().add(segment);

    for (WorkoutEvent event : workout.getWorkoutEvents()) {
      log.info("event = {}", event);
    }

    assertThat(workout.getWorkoutEvents().get(0), is(segment));
    assertThat(workout.getWorkoutEvents().get(1), is(lap));

  }

  @Test
  void daylightSavingsTimeChange() {
    String date1 = "2023-01-08 15:41:02 -0800";
    String date2 = "2023-01-08 16:41:02 -0700";
    ZonedDateTime zdt1 = ZonedDateTime.parse(date1, DTF);
    ZonedDateTime zdt2 = ZonedDateTime.parse(date1, DTF);
    log.info("zdt1 = {}", zdt1);
    log.info("zdt2 = {}", zdt2);
    log.info("zdt1 = {}", zdt1.toEpochSecond());
    log.info("zdt2 = {}", zdt2.toEpochSecond());

    assertThat(zdt1.toEpochSecond(), is(zdt2.toEpochSecond()));
  }

}
