package org.gpc4j.health.watch;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.DayData;
import org.gpc4j.health.watch.xml.HealthData;
import org.gpc4j.health.watch.xml.Record;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
class LoadData {

  @Test
  void loadData() {

    HealthData data = JAXB.unmarshal(new File("/Users/lh32469/Projects/GitHub/health-data/apple_health_export/export.xml"), HealthData.class);
    log.info("data.getRecords().size() = {}", data.getRecords().size());

    List<Record> swimmingRecords = data.getRecords().stream()
//        .filter(record -> record.getType().equals(SWIMMING))
        .collect(Collectors.toList());

    RavenBean rb = new RavenBean();
    rb.setRavenDB("http://dell-4290.local:5050");
    rb.postConstruct();

    IDocumentSession session = rb.getSession();
    log.info("got session");

    for (Record re : swimmingRecords) {
      session.store(re);
    }
    session.saveChanges();

    log.info("swimmingRecords = {}", swimmingRecords.size());
    log.info("swimmingRecords.get(8) = {}", swimmingRecords.get(8));

  }

  @Test
  void loadWorkouts() {

    HealthData data = JAXB.unmarshal(new File("/Users/lh32469/Projects/GitHub/health-data/apple_health_export/export.xml"), HealthData.class);

    RavenBean rb = new RavenBean();
    rb.setRavenDB("http://dell-4290.local:5050");
    rb.postConstruct();

    IDocumentSession session = rb.getSession();
    log.info("got session");

    data.getWorkouts().forEach(workout -> {
      session.store(workout);
    });

    session.saveChanges();
    log.info("data.getWorkouts().size() = {}", data.getWorkouts().size());

  }

  @Test
  void loadAsJson() throws IOException {

    File file = new File("/Users/lh32469/Projects/GitHub/health-data/apple_health_export/data.xml");

    XmlMapper mapper = new XmlMapper();
    String xml = inputStreamToString(new FileInputStream(file));
    Map map = mapper.readValue(xml, HashMap.class);

    log.info("map = {}", map);

//    Map root = mapper.readTree(new File("/Users/lh32469/Projects/GitHub/health-data/apple_health_export/data.xml"));
//    log.info("root.size() = {}", root.size());
//
//    log.info("root = {}", root);

    RavenBean rb = new RavenBean();
    rb.setRavenDB("http://dell-4290.local:5050");
    rb.postConstruct();

    IDocumentSession session = rb.getSession();
    log.info("got session");

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

    RavenBean rb = new RavenBean();
    rb.setRavenDB("http://dell-4290.local:5050");
    rb.postConstruct();

    IDocumentSession session = rb.getSession();
    log.info("got session");

    List<Record> swimmingRecords = session.query(Record.class)
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
