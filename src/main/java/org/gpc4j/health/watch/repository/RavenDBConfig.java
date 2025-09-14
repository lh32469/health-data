package org.gpc4j.health.watch.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
  @Value("${ravendb.url}")
  private String[] urls;

  @Bean
  public IDocumentStore documentStore() {
    DocumentStore store = new DocumentStore(urls, databaseName);

    // Configure Jackson ObjectMapper for proper DateTime handling
    ObjectMapper mapper = store.getConventions().getEntityMapper();
    mapper.registerModule(new JavaTimeModule());

    store.initialize();
    return store;
  }

}
