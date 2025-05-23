package org.gpc4j.health.watch.jsf.beans;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.WorkoutYear;
import org.gpc4j.health.watch.security.UserProvider;
import org.gpc4j.health.watch.xml.Workout;
import org.primefaces.component.chart.Chart;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScope
@Component("workoutBean")
@Slf4j
public class WorkoutBean implements Constants {

  static final String[] MONTHS = new DateFormatSymbols().getMonths();

  @Autowired
  RavenBean ravenBean;

  IDocumentSession session;

  @Autowired
  private UserProvider userProvider;

  @Autowired
  CookieBean cookieBean;

  List<WorkoutYear> workoutYears;

  WorkoutYear selectedYear;

  /**
   * Workouts by year graph.
   */
  LineChartModel yearsGraph;

  /**
   * Type of workout (Swimming/Walking) for Titles, Labels, etc..
   */
  private String workout;

  /**
   * Default JSF Template for desktop browsers.
   */
  @Getter
  private String template = "template.xhtml";

  /**
   * JSF Template to use for phones.
   */
  public static final String PHONE_TEMPLATE = "phone.xhtml";

  @PostConstruct
  public void postConstruct() {
    log.trace(this.toString());

    log.trace("cookieBean = {}", cookieBean);

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();
    log.trace("SessionMap = {}", externalContext.getSessionMap());

    Map<String, String> headerMap = externalContext.getRequestHeaderMap();
    log.trace("headerMap = {}", headerMap);
    final String userAgent = headerMap.getOrDefault("user-agent", "");
    log.trace("userAgent = {}", userAgent);

    if (userAgent.toLowerCase().contains("iphone")) {
      template = PHONE_TEMPLATE;
    }

    workout = cookieBean.getWorkout();
    session = ravenBean.getSession();
    workoutYears = new LinkedList<>();

    yearsGraph = new LineChartModel();
    yearsGraph.setTitle(workout + " Distance per Year by Month");
    yearsGraph.setLegendPosition("n");
    yearsGraph.getAxes().put(AxisType.X, new CategoryAxis("Months"));

    Axis yAxis = yearsGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Miles");
    yAxis.setMin(0);

    List<Workout> workouts = session.query(Workout.class)
        .whereEquals("user", userProvider.getUser().getUsername())
        .whereEquals("workoutActivityType", WORKOUT_MAP.get(workout))
        .selectFields(Workout.class,
            "duration", "totalDistance", "startDate", "totalEnergyBurned",
            "workoutActivityType", "workoutStatistics")
        .toList();

    // Get total active years for this workout
    Set<Integer> active = getActiveYears(workouts);

    if (PHONE_TEMPLATE.equals(template)) {
      active = active.stream()
          .sorted(Comparator.reverseOrder())
          .limit(5)  // Last 5 years on phone
          .collect(Collectors.toSet());
    }

    active.stream().sorted().forEach(year -> {

      List<Workout> filtered = workouts.stream()
          .filter(workout -> workout.getStartDate().startsWith(year + "-"))
          .collect(Collectors.toList());

      WorkoutYear workoutYear = new WorkoutYear(year, filtered);

      workoutYears.add(workoutYear);

      LineChartSeries yearChart = new LineChartSeries();
      yearChart.setLabel(String.valueOf(year));

      double total = 0;
      for (int i = 1; i <= 12; i++) {

        LocalDate date = LocalDate.of(year, i, 1);
        if (date.isAfter(LocalDate.now())) {
          break;
        }

        total += workoutYear.get(i).getDistance();
        String month = MONTHS[i - 1];
        yearChart.set(month, total);

      }

      yearsGraph.addSeries(yearChart);

      if (year == LocalDate.now().getYear()) {

        LineChartSeries projected = new LineChartSeries();
        projected.setLabel(year + " Projected");
        projected.setMarkerStyle("plus");
        projected.setShowLine(false);
//        projected.set(
//            new DateFormatSymbols().getMonths()[11],
//            workoutYear.getProjectedDistance());

        final double monthlyRate = workoutYear.getProjectedDistance() / 12.0;

        for (int i = 0; i < 12; i++) {

          LocalDate date = LocalDate.of(year, i + 1, 1);
          if (date.isAfter(LocalDate.now())) {
            projected.set(MONTHS[i], monthlyRate * (i + 1));
          }
        }

        if (projected.getData().isEmpty()) {
          log.debug("Projection complete for year = {}", year);
        } else {
          yearsGraph.addSeries(projected);
        }

      }

    });

    if (!workouts.isEmpty()) {
      selectedYear = workoutYears.get(0);
    }
  }

  @PreDestroy
  public void preDestroy() {
    log.trace(this.toString());
    session.close();
  }

  public List<WorkoutYear> getYears() {
    return workoutYears;
  }

  public void onRowSelect(SelectEvent<WorkoutYear> event) throws IOException {

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    String yearSelected = String.valueOf(event.getObject().getYear());
    log.debug("yearSelected = {}", yearSelected);

    externalContext.addResponseCookie("Test", "Test4", null);
    externalContext.addResponseCookie(YEAR_COOKIE_KEY, yearSelected, null);

    externalContext.redirect("year.xhtml");
  }

  public List<WorkoutYear> getWorkoutYears() {
    return workoutYears;
  }

  public void setWorkoutYears(List<WorkoutYear> workoutYears) {
    this.workoutYears = workoutYears;
  }

  public WorkoutYear getSelectedYear() {
    return selectedYear;
  }

  public void setSelectedYear(WorkoutYear selectedYear) {
    this.selectedYear = selectedYear;
  }

  public ChartModel getYearsGraph() {
    return yearsGraph;
  }

  public void itemSelect(ItemSelectEvent event) {

    log.debug("Item Index: {}, DataSet Index:{}",
        event.getItemIndex(), event.getDataSetIndex());

    Chart chart = (Chart) event.getComponent();
    LineChartModel model = (LineChartModel) chart.getModel();
    ChartSeries line = model.getSeries().get(event.getDataSetIndex());
    Map<Object, Number> data = line.getData();
    Double value = (Double) data.values().toArray()[event.getItemIndex()];

    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
        data.keySet().toArray()[event.getItemIndex()].toString() + ", " + line.getLabel(),
        String.format("%.2f Miles", value));

    FacesContext.getCurrentInstance().addMessage(null, msg);
  }

  /**
   * Get a Set of which years are present in the List of Workouts provided.
   */
  Set<Integer> getActiveYears(List<Workout> workouts) {
    Set<Integer> years = workouts.stream()
        .map(workout ->
            LocalDateTime.parse(workout.getStartDate(), DTF))
        .map(LocalDateTime::getYear)
        .collect(Collectors.toSet());

    log.debug("years = {}", years);
    return years;
  }

  public String getWorkout() {
    return workout;
  }

}
