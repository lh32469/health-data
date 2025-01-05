package org.gpc4j.health.watch.jsf.beans;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.health.watch.security.UserProvider;
import org.primefaces.event.MenuActionEvent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.model.menu.Submenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequestScope
@Component("menuBarBean")
@Data
@Slf4j
public class MenuBarBean implements Constants {

  public static final String SELECT_COMMAND = "#{menuBarBean.select}";

  private MenuModel model = new DefaultMenuModel();

  @Autowired
  private UserProvider userProvider;

  boolean adminUser;

  /**
   * Current UIViewRoot Id.
   */
  private String viewId;

  /**
   * Views where workouts menu should be visible.
   */
  public static final List<String> workoutMenuViews =
      Arrays.asList(
          "/workouts.xhtml",
          "/year.xhtml",
          "/month.xhtml",
          "/day.xhtml");

  @PostConstruct
  public void postConstruct() {
    log.debug("MenuBarBean.postConstruct");

    FacesContext facesContext = FacesContext.getCurrentInstance();
    viewId = facesContext.getViewRoot().getViewId();
    log.debug("viewId = {}", viewId);

    adminUser = userProvider.getUser().getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .collect(Collectors.toList())
        .contains("ROLE_ADMIN");

    log.debug("adminUser = {}", adminUser);

    MenuItem home = DefaultMenuItem.builder()
        .value("Home")
        .url("workouts.xhtml")
        .icon("pi pi-fw pi-home")
        .build();

    model.getElements().add(home);
    model.getElements().add(getFileMenu());
    if (adminUser) {
      model.getElements().add(getUsersMenu());
    }
    if (workoutMenuViews.contains(viewId)) {
      model.getElements().add(getWorkoutsMenu());
    }
  }

  Submenu getFileMenu() {

    DefaultSubMenu fileMenu = DefaultSubMenu.builder()
        .label("File")
        .icon("pi pi-fw pi-file")
        .build();

    DefaultMenuItem upload = DefaultMenuItem.builder()
        .value("Upload")
        .icon("pi pi-fw pi-cloud-upload")
        .ajax(false)
        .url("upload.xhtml")
        .build();
    fileMenu.getElements().add(upload);

    return fileMenu;
  }

  Submenu getUsersMenu() {

    Submenu users = DefaultSubMenu.builder()
        .label("Users")
        .icon("pi pi-fw pi-user")
        .build();

    DefaultMenuItem newUser = DefaultMenuItem.builder()
        .value("New")
        .icon("pi pi-fw pi-user-plus")
        .ajax(false)
        .url("createaccount.xhtml")
        .build();

    users.getElements().add(newUser);

    return users;

  }

  /**
   * Get Menu of which workoutActivityTypes are supported.
   */
  Submenu getWorkoutsMenu() {

    Submenu workouts = DefaultSubMenu.builder()
        .label("Workout")
        .icon("pi pi-fw pi-folder")
        .build();

    WORKOUT_MAP.keySet().forEach(workout -> {
      workouts.getElements().add(
          DefaultMenuItem.builder()
              .value(workout)
              .ajax(false)
              .command(SELECT_COMMAND)
              .build());
    });

    return workouts;
  }

  public void select(MenuActionEvent event) throws IOException {
    log.debug("event = {}", event);
    String workout = event.getMenuItem().getValue().toString();
    log.debug("workout = {}", workout);

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    externalContext.addResponseCookie(WORKOUT_COOKIE_KEY, workout, null);

    externalContext.redirect(viewId);
  }

  public MenuModel getModel() {
    return model;
  }

}
