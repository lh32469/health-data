package org.gpc4j.health.watch.security;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
public class UserDetailService implements UserDetailsService {

  public static final User INVALID = new User();

  @Autowired
  RavenBean ravenBean;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("username = {}", username);

    User user;

    try (IDocumentSession session = ravenBean.getSession()) {
      user = session.load(User.class, username);
      if (user == null) {
        log.info("username '{}' not found", username);
        user = INVALID;
      } else {
        log.info("found user = {}", user);
      }
    }

    return user;
  }

}
