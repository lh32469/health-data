package org.gpc4j.health.watch.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.db.dto.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Data
@Component
@RequestScope
public class UserProvider {

  User user;

  @PostConstruct
  public void postConstruct() {
    log.info("UserProvider.postConstruct");

    user = (User) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

    log.info("user = {}", user);
  }

  @PreDestroy
  public void preDestroy() {
    log.info("UserProvider.preDestroy");
  }

}
