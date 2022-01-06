package org.gpc4j.health.watch.jsf.beans;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.security.UserProvider;
import org.primefaces.component.menubar.Menubar;
import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.component.submenu.UISubmenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@RequestScope
@Component("menuBarBean")
@Data
@Slf4j
public class MenuBarBean {

  private Menubar menubar = new Menubar();

  @Autowired
  private UserProvider userProvider;

  boolean adminUser;

  @PostConstruct
  public void postConstruct() {
    log.debug("MenuBarBean.postConstruct");

    adminUser = userProvider.getUser().getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .collect(Collectors.toList())
        .contains("ROLE_ADMIN");

    log.debug("adminUser = {}", adminUser);

    menubar.getElements().add(getFileMenu());
    menubar.getElements().add(getUsersMenu());
  }

  UISubmenu getFileMenu() {

    UISubmenu file = new UISubmenu();
    file.setLabel("File");
    file.setIcon("pi pi-fw pi-file");

    UIMenuItem upload = new UIMenuItem();
    upload.setValue("Upload");
    upload.setOutcome("upload.xhtml");
    upload.setIcon("pi pi-fw pi-plus");
    file.getElements().add(upload);

    return file;

  }

  UISubmenu getUsersMenu() {

    UISubmenu users = new UISubmenu();
    users.setLabel("Users");
    users.setIcon("pi pi-fw pi-user");

    if (adminUser) {
      UIMenuItem newUser = new UIMenuItem();
      newUser.setValue("New");
      newUser.setIcon("pi pi-fw pi-user-plus");
      newUser.setOutcome("createaccount.xhtml");
      users.getElements().add(newUser);
    }

    return users;

  }

}
