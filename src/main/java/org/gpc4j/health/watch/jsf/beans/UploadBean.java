package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.BulkInsertOperation;
import net.ravendb.client.documents.IDocumentStore;
import org.gpc4j.health.watch.db.docs.HeartRateReading;
import org.gpc4j.health.watch.security.UserProvider;
import org.gpc4j.health.watch.xml.HealthData;
import org.gpc4j.health.watch.xml.Record;
import org.gpc4j.health.watch.xml.Workout;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXB;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.gpc4j.health.watch.jsf.beans.Constants.DTF;
import static org.gpc4j.health.watch.jsf.beans.Constants.HEART_RATE;
import static org.gpc4j.health.watch.xml.WorkoutDataProcessor.processData;

@RequestScope
@Component("uploadBean")
@Slf4j
public class UploadBean {

  /**
   * IDocumentStore from RavenDBConfig
   */
  @Autowired
  private IDocumentStore documentStore;

  BulkInsertOperation bulkStore;

  @Autowired
  UserProvider userProvider;

  @PostConstruct
  public void postConstruct() {
    log.info(this.toString());
    bulkStore = documentStore.bulkInsert();
  }

  @PreDestroy
  public void preDestroy() {
    log.info(this.toString());
    bulkStore.close();
  }

//  /**
//   * Handle posted export.zip file
//   */
//  public void handlePostUpload(MultipartFile file, User user) throws IOException {
//    log.info("file.getName() = {}", file.getOriginalFilename());
//    log.info("file.getSize() = {}", file.getSize());
//
//    ZipInputStream zis = new ZipInputStream(
//        new BufferedInputStream(file.getInputStream()));
//
//    ZipEntry entry;
//
//    while ((entry = zis.getNextEntry()) != null) {
//      if ("apple_health_export/export.xml".equals(entry.toString())) {
//        log.debug("Extracting: {}", entry);
//        HealthData data = JAXB.unmarshal(zis, HealthData.class);
//        zis.close();
//        postData(data, user);
//        break;
//      }
//    }
//
//  }

  public void handleFileUpload(FileUploadEvent event) throws IOException {

    UploadedFile uploadedFile = event.getFile();

    log.debug("uploadedFile.getFileName() = {}", uploadedFile.getFileName());

    if (uploadedFile.getFileName().endsWith(".zip")) {

      ZipInputStream zis = new ZipInputStream(
          new BufferedInputStream(uploadedFile.getInputStream()));

      ZipEntry entry;

      while ((entry = zis.getNextEntry()) != null) {
        if ("apple_health_export/export.xml".equals(entry.toString())) {
          log.debug("Extracting: {}", entry);
          HealthData data = JAXB.unmarshal(zis, HealthData.class);
          zis.close();
          postData(data);
          break;
        }
      }
    }

    if (uploadedFile.getFileName().endsWith(".xml")) {
      HealthData data = JAXB.unmarshal(
          uploadedFile.getInputStream(), HealthData.class);

//      /*
//       * Upload Records in sections.
//       */
//      List<Record> records = data.getRecords();
//      while (!records.isEmpty()) {
//
//        session = ravenBean.getSession();
//        log.debug("Got session for {} Records", records.size());
//
//        for (int i = 0; i < 1000; i++) {
//          if (!records.isEmpty()) {
//            Record record = records.remove(0);
//            record.setUser(user);
//            session.store(record, record.getId());
//          } else {
//            break;
//          }
//        }
//
//        session.saveChanges();
//        session.close();
//      }
//
//      log.info("Saved Records");

    }

    FacesMessage message = new FacesMessage("Successful",
                                            event.getFile()
                                                 .getFileName() + " is uploaded.");

    FacesContext.getCurrentInstance().addMessage(null, message);
  }

//  /**
//   * Handle posting data from user logged in to web page.
//   */
//  void postData(HealthData data) {
//    postData(data, userProvider.getUser());
//  }

  void postData(HealthData data) {

    log.debug("data.getWorkouts().size() = {}", data.getWorkouts().size());
    log.debug("data.getRecords().size() = {}", data.getRecords().size());

    final String user = userProvider.getUser().getUsername();

    // Add HeartRateReadings to Some Workouts
    processData(data);

    data.getWorkouts().stream()
        .peek(workout -> workout.setUser(user))
        .forEach(workout -> {
                   ZonedDateTime created =
                       ZonedDateTime.parse(workout.getCreationDate(), DTF);
                   log.debug("created workout = {} -> {} ",
                             workout.getCreationDate(), created);
                   long second = created.toEpochSecond();
                   bulkStore.store(workout, user + ".W." + second);
                 }
        );

    log.info("Saved {} Workouts", data.getWorkouts().size());

    Set<Record> unique = new HashSet<>(data.getRecords());
    if (unique.size() != data.getRecords().size()) {
      throw new IllegalStateException("Duplicate Records: " + user);
    }

//    // Save HeartRateReadings
//    unique.stream()
//          .filter(record -> record.getType()
//                                  .equals(HEART_RATE))
//          .map(entry -> {
//            HeartRateReading hr = new HeartRateReading();
//            hr.setUser(user);
//            hr.setDate(ZonedDateTime.parse(entry.getStartDate(), DTF));
//            hr.setValue(Float.parseFloat(entry.getValue()));
//            return hr;
//          })
//          .forEach(doc -> bulkStore.store(doc, doc.getId()));

  }

  public static void main(String[] args) {
    System.out.println("UploadBean.main");

    File file = new File("/tmp/export.xml");

    HealthData data = JAXB.unmarshal(file, HealthData.class);

    Workout workout0
        = data.getWorkouts().get(0);

    Workout swimming = data.getWorkouts().stream()
                           .filter(workout -> workout.getWorkoutActivityType()
                                                     .endsWith("Swimming"))
                           .findFirst()
                           .get();

//    System.out.println("swimming = " + swimming);

    System.out.println();
    System.out.println("swimming.getMetadataEntry() = " + swimming.getMetadataEntry());

//    System.out.println("workout0 = " + workout0);
//
//    System.out.println("\n\nworkout0.getMetadataEntry() size= " +
//        workout0.getMetadataEntry().size());
//    System.out.println("\n\nworkout0.getMetadataEntry() = " +
//        workout0.getMetadataEntry());
  }

}
