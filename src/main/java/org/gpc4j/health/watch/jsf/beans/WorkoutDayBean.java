package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.WorkoutDay;
import org.gpc4j.health.watch.security.UserProvider;
import org.gpc4j.health.watch.xml.Workout;
import org.gpc4j.health.watch.xml.WorkoutEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

@RequestScope
@Component("workoutDayBean")
@Named
@Slf4j
public class WorkoutDayBean implements Constants {

  @Autowired
  RavenBean ravenBean;

  @Autowired
  private UserProvider userProvider;

  @Autowired
  CookieBean cookieBean;
  /**
   * Get Year and Month from Cookies passed in via redirect from year.xhtml page.
   */
  private int year;
  private int month;
  private int day;

  /**
   * Workouts for the day.
   */
  List<Workout> workouts;

  /**
   * Totals  for the day.
   */
  private double totalMiles;
  private double calories;

  /**
   * The Chart/Graph object used by JSF/Primefaces.
   */
  LineChartModel dayGraph;

  @PostConstruct
  public void postConstruct() {
    log.debug("WorkoutDayBean.postConstruct");

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    year = cookieBean.getYear();
    month = cookieBean.getMonth();
    day = cookieBean.getDay();

    IDocumentSession session = ravenBean.getSession();

    // Get fully populated Workouts for this day and User
    String queryString = String.format("%d-%02d-%02d", year, month, day);
    log.debug("queryString = {}", queryString);
    workouts = session.query(Workout.class)
        .whereEquals("user", userProvider.getUser().getUsername())
        .whereStartsWith("startDate", queryString)
        .whereEquals("workoutActivityType", WORKOUT_MAP.get(cookieBean.getWorkout()))
        .toList();

    totalMiles = workouts.stream()
        .mapToDouble(Workout::getTotalDistance)
        .sum();

    calories = workouts.stream()
        .mapToDouble(Workout::getTotalEnergyBurned)
        .sum();

    log.info("{}-{}-{} = {}", year, month, day, workouts.size());

    dayGraph = new LineChartModel();
    dayGraph.setTitle("Segments/Sets");
    dayGraph.setLegendPosition("n");

    Axis yAxis = dayGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Seconds per Length");
    yAxis.setTickInterval("10");

    Axis xAxis = dayGraph.getAxis(AxisType.X);
    xAxis.setMin(0);
    xAxis.setLabel("Lengths");

    for (Workout workout : workouts) {
      int index = 1;
      LineChartSeries segment = null;
      for (WorkoutEvent event : workout.getWorkoutEvents()) {
        log.trace("event = {}", event);

        if (PAUSE_WORKOUT.equals(event.getType())) {
          continue;
        }

        if (RESUME_WORKOUT.equals(event.getType())) {
          continue;
        }

        if (SEGMENT.equals(event.getType())) {
          if (null != segment) {
            dayGraph.addSeries(segment);
          }
          // Start of new Segment/Set
          log.debug("New Segment = {}", event);
          segment = new LineChartSeries();
          segment.setShowMarker(false);

          // TODO: Make event.duration a double
          segment.setLabel(event.getDate() + "; " + event.getDurationF());
        } else {
          segment.set(index++, 60 * Double.parseDouble(event.getDuration()));
        }

      }

      // Add last Segment/Set
      dayGraph.addSeries(segment);
    }

  }

  public void onRowSelect(SelectEvent<WorkoutDay> event) throws IOException {

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    String monthSelected = String.valueOf(event.getObject().getDay());
    log.info("monthSelected = {}", monthSelected);

    externalContext.addResponseCookie(DAY_COOKIE_KEY, monthSelected, null);

    externalContext.redirect("day.xhtml");
  }

  public List<Workout> getWorkouts() {
    return workouts;
  }

  public LineChartModel getDayGraph() {
    return dayGraph;
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public String getDate() {
    LocalDate date = LocalDate.of(year, month, day);
    return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        .withLocale(Locale.US));
  }

  public int getDay() {
    return day;
  }

}
