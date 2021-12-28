package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.WorkoutMonth;
import org.gpc4j.health.watch.xml.Workout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequestScope
//@Component("yearBean")
@Slf4j
public class YearBean implements Constants {

  @Autowired
  RavenBean ravenBean;

  /**
   * Get Year from Session passed in via redirect from workouts.xhtml page.
   */
  private int year;

  List<WorkoutMonth> months;

  @PostConstruct
  public void postConstruct() {
    log.info("YearBean.postConstruct");

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();
    log.info("SessionMap = {}", externalContext.getSessionMap());

    year = (int) externalContext.getSessionMap().get(YEAR_COOKIE_KEY);
    log.info("year = {}", year);

    IDocumentSession session = ravenBean.getSession();

    // Get all the Workouts for this year
    List<Workout> workouts = session.query(Workout.class)
        .search("startDate", year + "-*")
        .andAlso()
        .whereEquals("workoutActivityType", SWIMMING_WORKOUT)
        .toList();

    // Divide into WorkoutMonths
    months = new LinkedList<>();

    for (int month = 1; month <= 12; month++) {
      String prefix = String.format("%d-%02d", year, month);
      log.debug("prefix = {}", prefix);

      List<Workout> workoutsForTheMonth = workouts.stream()
          .filter(workout -> workout.getStartDate().startsWith(prefix))
          .collect(Collectors.toList());

      WorkoutMonth workoutMonth = new WorkoutMonth(month, workoutsForTheMonth);
      months.add(workoutMonth);
    }
  }

  /**
   * Get the WorkoutMonth data in this year.
   *
   * @param month Jan -> 1, Feb -> 2, etc..
   */
  public WorkoutMonth get(int month) {
    return months.get(month - 1);
  }

  public Object getMonths() {
    return months;
  }

}
