package org.gpc4j.health.watch.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class AppGrantedAuthority implements GrantedAuthority {

  private String authority;

}
