package org.gpc4j.health.watch.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.gpc4j.health.watch.jsf.beans.Constants.SWIMMING_WORKOUT;
import static org.gpc4j.health.watch.xml.Record.DTF;

@Slf4j
@SpringBootTest
public class WorkoutIT {

  @Autowired
  RavenBean ravenBean;

  //  @Test
  public void getActiveYears() {
    log.info("log = {}", log);
    log.info("ravenBean = {}", ravenBean);

    IDocumentSession session = ravenBean.getSession();

    List<Workout> workouts = session.query(Workout.class)
        .whereEquals("workoutActivityType", SWIMMING_WORKOUT)
        .selectFields(Workout.class, "startDate")
        .toList();

    log.info("workouts.size() = {}", workouts.size());
    log.info("workouts.get(0) = {}", workouts.get(0));

    Set<Integer> years = workouts.stream()
        .map(workout ->
            LocalDateTime.parse(workout.getStartDate(), DTF))
        .map(LocalDateTime::getYear)
        .collect(Collectors.toSet());

    log.info("years = {}", years);

    years.stream().sorted().forEach(year -> log.info("year = {}", year));
  }

  @Test
  public void mapEventDate() {
    log.info("ravenBean = {}", ravenBean);

    String queryString = String.format("%d-%02d-%02d", 2022, 9, 16);
    log.info("queryString = {}", queryString);

    try (IDocumentSession session = ravenBean.getSession()) {
      List<Workout> workouts = session.query(Workout.class)
          .whereEquals("user","lth")
          .whereStartsWith("startDate", queryString)
          .whereEquals("workoutActivityType", SWIMMING_WORKOUT)
          .toList();

      for (Workout workout : workouts) {

        for (WorkoutEvent event : workout.getWorkoutEvents()) {
          log.info("event = {}", event);
        }
      }

    }
  }


  @Test
  public void workoutEventSorting() throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();

    String segmentString = "{\n" +
        "            \"type\": \"HKWorkoutEventTypeSegment\",\n" +
        "            \"date\": \"2022-09-16 15:27:45 -0700\",\n" +
        "            \"duration\": \"32.43460502028465\",\n" +
        "            \"durationUnit\": \"min\"\n" +
        "        }";
    WorkoutEvent segment = mapper.readValue(segmentString, WorkoutEvent.class);

  }
}
