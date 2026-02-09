package org.gpc4j.health.watch;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.BulkInsertOperation;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.docs.HeartRateReading;
import org.gpc4j.health.watch.db.dto.DayData;
import org.gpc4j.health.watch.repository.RavenDBConfig;
import org.gpc4j.health.watch.xml.HealthData;
import org.gpc4j.health.watch.xml.Record;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gpc4j.health.watch.jsf.beans.Constants.HEART_RATE;
import static org.gpc4j.health.watch.jsf.beans.Constants.LAP;
import static org.gpc4j.health.watch.jsf.beans.Constants.SWIMMING_WORKOUT;
import static org.gpc4j.health.watch.xml.Record.DTF;

@Slf4j
class LoadData {

  private IDocumentStore documentStore;
  IDocumentSession session;
  private BulkInsertOperation bulkStore;

  @BeforeEach
  void setup() {
    log.info("setup");

    RavenDBConfig ravenDBConfig = new RavenDBConfig();
    ravenDBConfig.setUrls(List.of("http://nodea:8080"));
    ravenDBConfig.setDatabaseName("HealthData");

    documentStore = ravenDBConfig.documentStore();
    session = documentStore.openSession();
    bulkStore = documentStore.bulkInsert();
  }

  @AfterEach
  void tearDown() {
    log.info("tearDown");
    session.close();
    documentStore.close();
  }

  @Test
  void loadHeartrate() {

    HealthData data = JAXB.unmarshal(new File(
                                         "/Users/lh32469/Projects/GitHub/health" +
                                             "-data/apple_health_export/export.xml"),
                                     HealthData.class);
    log.info("data.getRecords().size() = {}", data.getRecords().size());

    String type = "HKQuantityTypeIdentifierHeartRate";

    List<Record> heartrates =
        data.getRecords().stream()
            .filter(record -> record.getType().equals(type))
            .collect(Collectors.toList());

    log.info("heartrates.size() = {}", heartrates.size());

    List<HeartRateReading> docs =
        heartrates.stream()

                  .map(entry -> {
                    HeartRateReading hr = new HeartRateReading();
                    hr.setUser("JUnit");
                    hr.setDate(ZonedDateTime.parse(entry.getStartDate(), DTF));
                    hr.setValue(Float.parseFloat(entry.getValue()));
                    return hr;
                  })
                  .collect(Collectors.toList());

    log.info("docs.size() = {}", docs.size());

    try (BulkInsertOperation bulk = documentStore.bulkInsert()) {
      log.info("Saving..." + bulk);
      for (HeartRateReading doc : docs) {
        bulk.store(doc, doc.getId());
      }
    }

    log.info("Saved");


  }

  @Test
  void loadData() {

    HealthData data = JAXB.unmarshal(new File(
                                         "/Users/lh32469/Projects/GitHub/health" +
                                             "-data/apple_health_export/export.xml"),
                                     HealthData.class);
    log.info("data.getRecords().size() = {}", data.getRecords().size());

    List<Record> swimmingRecords = data.getRecords().stream()
//        .filter(record -> record.getType().equals(SWIMMING))
                                       .collect(Collectors.toList());

    for (Record re : swimmingRecords) {
      session.store(re);
    }
    session.saveChanges();

    log.info("swimmingRecords = {}", swimmingRecords.size());
    log.info("swimmingRecords.get(8) = {}", swimmingRecords.get(8));

  }

  @Test
  void parseCreationDate() {
    String date = "2021-12-12 12:30:49 -0800";
    LocalDateTime start = LocalDateTime.parse(date, DTF);
    log.info("start = {}", start);
    long foo = start.toEpochSecond(ZoneOffset.UTC);
    log.info("foo = {}", foo);
  }

  @Test
  void loadWorkouts() {
    log.info("started");

    HealthData data = JAXB.unmarshal(new File(
                                         "/Users/lh32469/Projects/GitHub/health" +
                                             "-data/apple_health_export/export.xml"),
                                     HealthData.class);
    log.info("Loaded {} Records", data.getRecords().size());
    log.info("Loaded {} Workouts", data.getWorkouts().size());

    List<Record> heartrates =
        data.getRecords().parallelStream()
            .filter(record -> record.getType().equals(HEART_RATE))
            .collect(Collectors.toList());

    log.info("Filtered {} Heart Rate Records", heartrates.size());

    data.getWorkouts()
        .parallelStream()
        .filter(workout ->
                    workout.getWorkoutActivityType().equals(SWIMMING_WORKOUT))
//        .limit(10)
        .forEach(workout -> {
          workout.setUser("JUnit");
          log.info("Processing Workout starting: {}", workout.getStart());

          // Get heart rates for duration of workout
          List<Record> heartRatesForWorkout =
              heartrates.stream()
                        .filter(record ->
                                    record.getStart()
                                          .isAfter(
                                              workout.getStart()))
                        .filter(record ->
                                    record.getEnd()
                                          .isBefore(workout.getEnd()))
                        .collect(Collectors.toList());

          workout.getWorkoutEvents()
                 .stream()
                 .filter(event -> event.getType().equals(LAP))
                 .forEach(event -> {
                   LocalDateTime date = LocalDateTime.parse(event.getDate(), DTF);
                   Optional<Record> heartRateRecord =
                       findHeartRate(heartRatesForWorkout, date);

                   if (heartRateRecord.isPresent()) {
//                     log.info("Found heart rate for workout = " + event);

                     Record record = heartRateRecord.get();
                     float value = Float.parseFloat(record.getValue());
                     short rate = (short) value;
                     event.setHeartRate(rate);
                   } else {
//                     log.warn("Could not find heart rate for workout = " + event);
                   }

                 });
          synchronized (bulkStore) {
            // Bulk Insert store methods cannot be executed concurrently
            bulkStore.store(workout);
          }
        });

    log.info("data.getWorkouts().get(0) = {}", data.getWorkouts().get(0));
  }

  Optional<Record> findHeartRate(List<Record> heartrates, LocalDateTime start) {

    return heartrates.stream()
                     .filter(record ->
                                 record.getStart().isAfter(start))
                     .filter(record ->
                                 record.getEnd().isBefore(start.plusMinutes(1)))
                     .findFirst();
  }

  @Test
  void loadAsJson() throws IOException {

    File file = new File(
        "/Users/lh32469/Projects/GitHub/health-data/apple_health_export/data.xml");

    XmlMapper mapper = new XmlMapper();
    String xml = inputStreamToString(new FileInputStream(file));
    Map map = mapper.readValue(xml, HashMap.class);

    log.info("map = {}", map);

//    Map root = mapper.readTree(new File
//    ("/Users/lh32469/Projects/GitHub/health-data/apple_health_export/data.xml"));
//    log.info("root.size() = {}", root.size());
//
//    log.info("root = {}", root);

    session.store(map);
    session.saveChanges();
  }

  public String inputStreamToString(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    return sb.toString();
  }

  @Test
  void loadDay() {

    List<Record> swimmingRecords =
        session.query(Record.class)
               .search("startDate", "2021-11-21*")
               .orderBy("startDate")
               .toList();

    Collections.sort(swimmingRecords, Collections.reverseOrder());

    log.info("swimmingRecords = {}", swimmingRecords.size());
    log.info("swimmingRecords.get(8) = {}", swimmingRecords.get(8));
    for (Record record : swimmingRecords) {
      log.info("record = {}", record);
    }

    DayData dayData = new DayData();
    dayData.setRecords(swimmingRecords);

    log.info("dayData.time() = {}", dayData.time() / 60.0);
  }

}
