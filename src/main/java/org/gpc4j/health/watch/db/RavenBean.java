package org.gpc4j.health.watch.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class RavenBean {

  @Setter
  @Value("${ravendb.urls}")
  private List<String> urls;

  private IDocumentStore docStore;

  @PostConstruct
  public void postConstruct() {
    log.info("URLs: {}", urls);
    docStore = new DocumentStore(urls.toArray(new String[0]), "HealthData");
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

}
