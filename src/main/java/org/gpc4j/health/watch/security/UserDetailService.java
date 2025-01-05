package org.gpc4j.health.watch.security;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.concurrent.TimeUnit;

@Slf4j
public class UserDetailService implements UserDetailsService {

  public static final User INVALID = new User();

  static final long CACHE_TIMEOUT = TimeUnit.SECONDS.toMillis(20);

  private final PassiveExpiringMap<String, User> cache =
      new PassiveExpiringMap<>(CACHE_TIMEOUT);

  @Autowired
  RavenBean ravenBean;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("username = {}", username);

    if (!cache.containsKey(username)) {

      try (IDocumentSession session = ravenBean.getSession()) {
        User user = session.load(User.class, username);
        if (user == null) {
          log.warn("username '{}' not found", username);
          return INVALID;
        } else {
          log.debug("found user = {}", user);
          cache.put(username, user);
        }
      }
    }

    return cache.get(username);
  }

}
