package org.gpc4j.health.watch.db.dto;

import lombok.Data;
import lombok.ToString;
import org.gpc4j.health.watch.security.AppGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.LinkedList;
import java.util.List;

@Data
public class User implements UserDetails {

  private String id;
  private String username;
  @ToString.Exclude
  private String password;
  private boolean enabled = true;
  private boolean accountNonExpired = true;
  private boolean accountNonLocked = true;
  private boolean credentialsNonExpired = true;
  private LinkedList<AppGrantedAuthority> authorities = new LinkedList<>();

  public void setUsername(String username) {
    this.username = username;
    this.id = username;
  }

  public void setId(String id) {
    setUsername(id);
  }

}
