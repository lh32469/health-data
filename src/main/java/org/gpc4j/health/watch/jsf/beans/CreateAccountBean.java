package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.User;
import org.gpc4j.health.watch.security.AppGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import static org.gpc4j.health.watch.security.UserDetailService.INVALID;

@RequestScope
@Component("createAccountBean")
@Slf4j
public class CreateAccountBean {

  private String username;
  private String password;

  static BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

  @Autowired
  RavenBean ravenBean;

  @Autowired
  UserDetailsService userDetailsService;

  public void username(ValueChangeEvent event) throws AbortProcessingException {
    log.debug("username event = {}", event);
    username = event.getNewValue().toString().trim();
  }

  public void password(ValueChangeEvent event) throws AbortProcessingException {
    log.debug("password event = {}", event);
    password = event.getNewValue().toString().trim();
  }

  public void submit(ActionEvent event) {
    log.info("event = {}", event);
    log.debug("username = {}", username);
    log.debug("password = {}", password);

    User user = (User) userDetailsService.loadUserByUsername(username);
    if (user != INVALID) {
      FacesMessage message = new FacesMessage(
          FacesMessage.SEVERITY_WARN,
          "Failed",
          "Username already exists");
      FacesContext.getCurrentInstance().addMessage(null, message);
    } else {

      AppGrantedAuthority basic = new AppGrantedAuthority();
      basic.setAuthority("ROLE_USER");

      user = new User();
      user.setId(username);
      user.setPassword(ENCODER.encode(password));
      user.getAuthorities().add(basic);

      try (IDocumentSession session = ravenBean.getSession()) {
        session.store(user);
        session.saveChanges();
      }

      FacesMessage message = new FacesMessage(
          FacesMessage.SEVERITY_INFO,
          "Created",
          "Username " + username + " created");
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
  }

}
