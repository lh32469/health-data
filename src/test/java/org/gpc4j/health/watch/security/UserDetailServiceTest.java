package org.gpc4j.health.watch.security;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
class UserDetailServiceTest {

  BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  IDocumentSession session;

  @BeforeEach
  void setup() {
    log.info("UserDetailServiceTest.setup");
    RavenBean rb = new RavenBean();
    rb.setRavenDB("http://dell-4290.local:5050");
    rb.postConstruct();

    session = rb.getSession();
    log.info("got session");
  }

  @AfterEach
  void tearDown() {
    log.info("UserDetailServiceTest.tearDown");
    session.close();
  }

//  @Test
  void createUser() {
    User user = new User();
    user.setId("lth");
    user.setPassword(encoder.encode("delth"));
    session.store(user);
    session.saveChanges();

  }

}
