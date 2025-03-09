package org.gpc4j.health.watch.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class RavenBean {

  @Value("${ravendb.url}")
  String ravenDB;

  private IDocumentStore docStore;

  @PostConstruct
  public void postConstruct() {
    log.info("ravenDB = " + ravenDB);
    docStore = new DocumentStore(ravenDB, "HealthData");
    docStore.initialize();

    ObjectMapper mapper = docStore.getConventions().getEntityMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Double.class, new CustomDoubleSerializer());
    mapper.registerModule(module);
  }

  public IDocumentStore getDocStore() {
    return docStore;
  }

  public IDocumentSession getSession() {
    return docStore.openSession();
  }

  public void setRavenDB(String ravenDB) {
    this.ravenDB = ravenDB;
  }

}
