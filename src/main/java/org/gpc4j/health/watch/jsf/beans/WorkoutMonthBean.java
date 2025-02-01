package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.WorkoutDay;
import org.gpc4j.health.watch.security.UserProvider;
import org.gpc4j.health.watch.xml.Workout;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequestScope
@Component("workoutMonthBean")
@Slf4j
public class WorkoutMonthBean implements Constants {

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

  /**
   * Totals for the Month.
   */
  private double totalMiles;
  private double calories;

  private List<WorkoutDay> days;
  private WorkoutDay selectedDay;

  public LineChartModel getDaysGraph() {
    return monthsGraph;
  }

  /**
   * Workouts by day graph.
   */
  LineChartModel monthsGraph;

  @PostConstruct
  public void postConstruct() {
    log.debug("WorkoutMonthBean.postConstruct");

    year = cookieBean.getYear();
    month = cookieBean.getMonth();

    IDocumentSession session = ravenBean.getSession();

    // Get all the Workouts for this year, month and user
    String queryString = String.format("%d-%02d-", year, month);
    List<Workout> workouts = session.query(Workout.class)
        .whereEquals("user", userProvider.getUser().getUsername())
        .whereStartsWith("startDate", queryString)
        .whereEquals("workoutActivityType", WORKOUT_MAP.get(cookieBean.getWorkout()))
        .selectFields(Workout.class,
            "duration", "totalDistance", "startDate", "totalEnergyBurned",
            "workoutActivityType", "workoutStatistics")
        .toList();

    workouts.forEach(workout -> {
      totalMiles += workout.getTotalDistance();
      calories += workout.getTotalEnergyBurned();
    });

    log.info("{}-{} = {}", year, month, workouts.size());

    String monthString = new DateFormatSymbols().getMonths()[month - 1];

    monthsGraph = new LineChartModel();
    monthsGraph.setTitle(cookieBean.getWorkout() +
        " Distance by Day for " + monthString + ", " + year);
    monthsGraph.setLegendPosition("n");
    monthsGraph.getAxes().put(AxisType.X, new CategoryAxis("Days"));

    Axis yAxis = monthsGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Miles");
    yAxis.setMin(0);
    long max = Math.max(20, Math.round((totalMiles / 10.0) + 1.25) * 10);
    yAxis.setMax(max);
    yAxis.setTickInterval("10");

    LineChartSeries lineGraph = new LineChartSeries();
    BarChartSeries barGraph = new BarChartSeries();
    barGraph.setLabel("Daily Distance");
    lineGraph.setLabel("Cumulative Distance");
    lineGraph.setShowMarker(false);

    // This order is important otherwise barchart data is off by one.  Dunno..
    monthsGraph.addSeries(barGraph);
    monthsGraph.addSeries(lineGraph);

    // Divide into WorkoutDays
    days = new LinkedList<>();

    // Running runningTotal for this month
    double runningTotal = 0;

    YearMonth yearMonthObject = YearMonth.of(year, month);
    int daysInMonth = yearMonthObject.lengthOfMonth();

    for (int day = 1; day <= daysInMonth; day++) {
      String prefix = String.format("%d-%02d-%02d", year, month, day);

      LocalDate date = LocalDate.of(year, month, day);
      if (date.isAfter(LocalDate.now())) {
        break;
      }

      List<Workout> workoutsForTheDay = workouts.stream()
          .filter(workout -> workout.getStartDate().startsWith(prefix))
          .collect(Collectors.toList());
      log.debug("{} = {}", prefix, workoutsForTheDay.size());

      WorkoutDay workoutDay = new WorkoutDay(day, workoutsForTheDay);
      if (!workoutDay.getWorkouts().isEmpty()) {
        days.add(workoutDay);
      }

      runningTotal += workoutDay.getDistance();

      lineGraph.set(day, runningTotal);
      barGraph.set(day, workoutDay.getDistance());
    }

  }

  public List<WorkoutDay> getDays() {
    return days;
  }

  public int getYear() {
    return year;
  }

  public String getDistance() {
    return String.format("%4.2f", totalMiles);
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

    String daySelected = String.valueOf(event.getObject().getDay());
    log.info("daySelected = {}", daySelected);

    externalContext.addResponseCookie(DAY_COOKIE_KEY, daySelected, null);

    externalContext.redirect("day.xhtml");
  }

  public void itemSelect(ItemSelectEvent event) {
    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
        "Item Index: " + event.getItemIndex() + ", DataSet Index:" + event.getDataSetIndex());

    FacesContext.getCurrentInstance().addMessage(null, msg);
  }

}
