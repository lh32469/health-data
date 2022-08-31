package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.security.UserProvider;
import org.gpc4j.health.watch.xml.HealthData;
import org.gpc4j.health.watch.xml.Record;
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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.gpc4j.health.watch.jsf.beans.Constants.DTF;

@RequestScope
@Component("uploadBean")
@Slf4j
public class UploadBean {

  @Autowired
  RavenBean ravenBean;

  IDocumentSession session;

  @Autowired
  UserProvider userProvider;

  @PostConstruct
  public void postConstruct() {
    log.info("UploadBean.postConstruct");
  }

  @PreDestroy
  public void preDestroy() {
    session.close();
  }

  public void handleFileUpload(FileUploadEvent event) throws IOException {

    UploadedFile uploadedFile = event.getFile();

    log.info("uploadedFile.getFileName() = {}", uploadedFile.getFileName());

    if (uploadedFile.getFileName().endsWith(".xml")) {
      HealthData data = JAXB.unmarshal(
          uploadedFile.getInputStream(), HealthData.class);

      log.info("data.getWorkouts().size() = {}", data.getWorkouts().size());
      log.info("data.getRecords().size() = {}", data.getRecords().size());

      session = ravenBean.getSession();
      log.info("Got session for Workouts");

      final String user = userProvider.getUser().getUsername();

      data.getWorkouts().stream()
          .peek(workout -> workout.setUser(user))
          .forEach(workout -> {
                LocalDateTime created = LocalDateTime.parse(workout.getCreationDate(), DTF);
                log.debug("created workout = {} -> {} ", workout.getCreationDate(), created);
                long second = created.toEpochSecond(ZoneOffset.UTC);
                session.store(workout, user + ".W." + second);
              }
          );

      session.saveChanges();
      log.info("Saved {} Workouts", data.getWorkouts().size());
      session.close();

      Set<Record> unique = new HashSet<>(data.getRecords());
      if (unique.size() != data.getRecords().size()) {
        throw new IllegalStateException("Duplicate Records: " + user);
      }

      /*
       * Upload Records in sections.
       */
      List<Record> records = data.getRecords();
      while (!records.isEmpty()) {

        session = ravenBean.getSession();
        log.debug("Got session for {} Records", records.size());

        for (int i = 0; i < 1000; i++) {
          if (!records.isEmpty()) {
            Record record = records.remove(0);
            record.setUser(user);
            session.store(record, record.getId());
          } else {
            break;
          }
        }

        session.saveChanges();
        session.close();
      }

      log.info("Saved Records");

    }

    FacesMessage message = new FacesMessage("Successful",
        event.getFile().getFileName() + " is uploaded.");

    FacesContext.getCurrentInstance().addMessage(null, message);
  }

}
