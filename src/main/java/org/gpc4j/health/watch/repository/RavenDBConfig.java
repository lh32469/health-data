package org.gpc4j.health.watch.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.client.http.ServerNode;
import org.gpc4j.health.watch.db.CustomDoubleSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

/**
 * Configuration class for RavenDB.
 */
@Slf4j
@Configuration
public class RavenDBConfig {

  @Setter
  @Value("${ravendb.database}")
  private String databaseName;

  @Setter
  @Value("${ravendb.urls}")
  private List<String> urls;

  @Bean
  public IDocumentStore documentStore() {
    log.info("URLs: {}", urls);

    DocumentStore store = new DocumentStore(urls.toArray(new String[0]), databaseName);

    // Configure Jackson ObjectMapper for proper DateTime handling
    ObjectMapper mapper = store.getConventions().getEntityMapper();
    mapper.registerModule(new JavaTimeModule());

    SimpleModule module = new SimpleModule();
    // Format Doubles as Strings in to 4 decimal places
    module.addSerializer(Double.class, new CustomDoubleSerializer());
    mapper.registerModule(module);

    store.initialize();
    return store;
  }

  @Bean(destroyMethod = "close")
  @RequestScope
  public IDocumentSession session(IDocumentStore store) {
    IDocumentSession session = store.openSession();
    if (log.isDebugEnabled()) {
      ServerNode currentNode = session.advanced().getCurrentSessionNode();
      log.debug("Read from node: " + currentNode.getClusterTag());
    }
    return session;
  }

}
