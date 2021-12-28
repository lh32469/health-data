package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.xml.HealthData;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;

@RequestScope
@Component("uploadBean")
@Slf4j
public class UploadBean {

  @Autowired
  RavenBean ravenBean;

  @PostConstruct
  public void postConstruct() {
    log.info("UploadBean.postConstruct");
  }

  public void handleFileUpload(FileUploadEvent event) throws IOException {

    UploadedFile uploadedFile = event.getFile();

    log.info("uploadedFile.getFileName() = {}", uploadedFile.getFileName());

    if(uploadedFile.getFileName().endsWith(".xml")) {
      HealthData data = JAXB.unmarshal(
          uploadedFile.getInputStream(), HealthData.class);

      IDocumentSession session = ravenBean.getSession();
      log.info("got session");

      data.getWorkouts().forEach(session::store);

      session.saveChanges();
      log.info("data.getWorkouts().size() = {}", data.getWorkouts().size());
    }

    FacesMessage message = new FacesMessage("Successful",
        event.getFile().getFileName() + " is uploaded.");

    FacesContext.getCurrentInstance().addMessage(null, message);
  }

}
