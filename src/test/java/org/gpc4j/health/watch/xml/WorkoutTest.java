package org.gpc4j.health.watch.xml;

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
public class WorkoutTest {

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

}
