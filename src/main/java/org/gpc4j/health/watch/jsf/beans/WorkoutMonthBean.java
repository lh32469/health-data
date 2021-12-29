package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.WorkoutDay;
import org.gpc4j.health.watch.xml.Workout;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.LineChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestScope
@Component("workoutMonthBean")
@Slf4j
public class WorkoutMonthBean implements Constants {

  @Autowired
  RavenBean ravenBean;

  /**
   * Get Year and Month from Session passed in via redirect from year.xhtml page.
   */
  private int year;
  private int month;

  /**
   * Totals  for the month.
   */
  private double distance;
  private double calories;

  private List<WorkoutDay> days;
  private WorkoutDay selectedDay;

  public LineChartModel getMonthsGraph() {
    return monthsGraph;
  }

  /**
   * Workouts by month graph.
   */
  LineChartModel monthsGraph;

  @PostConstruct
  public void postConstruct() {
    log.info("WorkoutMonthBean.postConstruct");

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    Map<String, Object> cookieMap = externalContext.getRequestCookieMap();

    Cookie cookie = (Cookie) cookieMap.get(YEAR_COOKIE_KEY);
    year = Integer.parseInt(cookie.getValue());
    Cookie monthCookie = (Cookie) cookieMap.get(MONTH_COOKIE_KEY);
    month = Integer.parseInt(monthCookie.getValue());

    IDocumentSession session = ravenBean.getSession();

    // Get all the Workouts for this year and month
    String queryString = String.format("%d-%02d-*", year, month);
    List<Workout> workoutsForTheMonth = session.query(Workout.class)
        .search("startDate", queryString)
        .andAlso()
        .whereEquals("workoutActivityType", SWIMMING_WORKOUT)
        .selectFields(Workout.class, "duration", "totalDistance",
            "startDate", "totalEnergyBurned")
        .toList();

    distance = workoutsForTheMonth.stream()
        .mapToDouble(Workout::getTotalDistance)
        .sum();

    calories = workoutsForTheMonth.stream()
        .mapToDouble(Workout::getTotalEnergyBurned)
        .sum();

    log.info("{}-{} = {}", year, month, workoutsForTheMonth.size());

    // Divide into WorkoutDays
    days = new LinkedList<>();

    YearMonth yearMonthObject = YearMonth.of(year, month);
    int daysInMonth = yearMonthObject.lengthOfMonth();

    for (int day = 1; day <= daysInMonth; day++) {
      String prefix = String.format("%d-%02d-%02d", year, month, day);

      List<Workout> workoutsForTheDay = workoutsForTheMonth.stream()
          .filter(workout -> workout.getStartDate().startsWith(prefix))
          .collect(Collectors.toList());
      log.info("{} = {}", prefix, workoutsForTheDay.size());

      WorkoutDay workoutDay = new WorkoutDay(day, workoutsForTheDay);
      if (!workoutDay.getWorkouts().isEmpty()) {
        days.add(workoutDay);
      }
    }

  }

  public List<WorkoutDay> getDays() {
    return days;
  }

  public int getYear() {
    return year;
  }

  public String getDistance() {
    return String.format("%4.2f", distance);
  }

  public String getCalories() {
    return String.format("%6.2f", calories);
  }

  public String getMonth() {
    return new DateFormatSymbols().getMonths()[month - 1];
  }

  public WorkoutDay getSelectedDay() {
    return selectedDay;
  }

  public void setSelectedDay(WorkoutDay selectedDay) {
    this.selectedDay = selectedDay;
  }

  public void onRowSelect(SelectEvent<WorkoutDay> event) throws IOException {

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    String monthSelected = String.valueOf(event.getObject().getDay());
    log.info("monthSelected = {}", monthSelected);

    externalContext.addResponseCookie(MONTH_COOKIE_KEY, monthSelected, null);

    externalContext.redirect("month.xhtml");
  }

  public void itemSelect(ItemSelectEvent event) {
    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
        "Item Index: " + event.getItemIndex() + ", DataSet Index:" + event.getDataSetIndex());

    FacesContext.getCurrentInstance().addMessage(null, msg);
  }

}
