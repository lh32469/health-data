package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.dto.WorkoutMonth;
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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequestScope
@Component("workoutYearBean")
@Slf4j
public class WorkoutYearBean implements Constants {

  @Autowired
  IDocumentSession session;

  @Autowired
  private UserProvider userProvider;

  @Autowired
  CookieBean cookieBean;

  /**
   * Get Year from Session passed in via redirect from workouts.xhtml page.
   */
  private int year;

  /**
   * Totals for the Year.
   */
  private double totalMiles;
  private double totalCalories;

  private List<WorkoutMonth> months;
  private WorkoutMonth selectedMonth;

  public LineChartModel getMonthsGraph() {
    return monthsGraph;
  }

  /**
   * Workouts by month graph.
   */
  LineChartModel monthsGraph;

  @PostConstruct
  public void postConstruct() {
    log.debug(this.toString());

    year = cookieBean.getYear();

    String workoutType = cookieBean.getWorkout();
    log.debug("year = {}, workoutType = {}", year, workoutType);

    // Get all the Workouts for this year for current User
    List<Workout> workouts = session.query(Workout.class)
        .whereEquals("user", userProvider.getUser().getUsername())
        .whereStartsWith("startDate", year + "-")
        .whereEquals("workoutActivityType", WORKOUT_MAP.get(workoutType))
        .selectFields(Workout.class,
            "user", "duration", "totalDistance", "startDate", "totalEnergyBurned",
            "workoutActivityType", "workoutStatistics")
        .toList();

    log.debug("workouts for the year = {}", workouts.size());

    workouts.forEach(workout -> {
      totalMiles += workout.getTotalDistance();
      totalCalories += workout.getTotalEnergyBurned();
    });

    monthsGraph = new LineChartModel();
    monthsGraph.setTitle(workoutType + " Distance by Month for " + year);
    monthsGraph.setLegendPosition("n");
    monthsGraph.getAxes().put(AxisType.X, new CategoryAxis("Months"));

    Axis yAxis = monthsGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Miles");
    yAxis.setMin(0);
    long max = Math.max(150, Math.round((totalMiles / 10.0) + 1.25) * 10);
    yAxis.setMax(max);
    yAxis.setTickInterval("10");

    LineChartSeries cumulativeChart = new LineChartSeries();
    BarChartSeries barGraph = new BarChartSeries();
    barGraph.setLabel("Monthly Distance");
    cumulativeChart.setLabel("Cumulative Distance");

    // This order is important otherwise barchart data is off by one.  Dunno..
    monthsGraph.addSeries(barGraph);
    monthsGraph.addSeries(cumulativeChart);

    // Divide into WorkoutMonths
    months = new LinkedList<>();

    // Running runningTotal for this year
    double runningTotal = 0;

    for (int month = 1; month <= 12; month++) {
      String prefix = String.format("%d-%02d", year, month);
      log.debug("prefix = {}", prefix);

      LocalDate date = LocalDate.of(year, month, 1);
      if (date.isAfter(LocalDate.now())) {
        break;
      }

      List<Workout> workoutsForTheMonth = workouts.stream()
          .filter(workout -> workout.getStartDate().startsWith(prefix))
          .collect(Collectors.toList());

      WorkoutMonth workoutMonth = new WorkoutMonth(month, workoutsForTheMonth);
      months.add(workoutMonth);

      runningTotal += workoutMonth.getDistance();
      String monthString = new DateFormatSymbols().getMonths()[month - 1];

      log.debug("{} = {}", monthString, workoutMonth.getDistance());

      cumulativeChart.set(monthString, runningTotal);
      barGraph.set(monthString, workoutMonth.getDistance());

      selectedMonth = months.get(0);
    }

  }

  public List<WorkoutMonth> getMonths() {
    return months;
  }

  public void setSelectedMonth(WorkoutMonth selectedMonth) {
    this.selectedMonth = selectedMonth;
  }

  public WorkoutMonth getSelectedMonth() {
    return selectedMonth;
  }

  public int getYear() {
    return year;
  }

  public void onRowSelect(SelectEvent<WorkoutMonth> event) throws IOException {

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    String monthSelected = String.valueOf(event.getObject().getMonth());
    log.info("monthSelected = {}", monthSelected);

    externalContext.addResponseCookie(MONTH_COOKIE_KEY, monthSelected, null);

    externalContext.redirect("month.xhtml");
  }

  public void itemSelect(ItemSelectEvent event) {
    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
        "Item Index: " + event.getItemIndex() + ", DataSet Index:" + event.getDataSetIndex());

    FacesContext.getCurrentInstance().addMessage(null, msg);
  }

  public double getTotalMiles() {
    return totalMiles;
  }

  public String getTotalCaloriesAsString() {
    return String.format("%.2f", totalCalories);
  }

  public String getTotalMilesAsString() {
    return String.format("%.2f", totalMiles);
  }

}
