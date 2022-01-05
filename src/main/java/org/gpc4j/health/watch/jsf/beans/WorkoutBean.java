package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.WorkoutYear;
import org.gpc4j.health.watch.security.UserProvider;
import org.gpc4j.health.watch.xml.Workout;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartModel;
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
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.gpc4j.health.watch.xml.Record.DTF;

@RequestScope
@Component("workoutBean")
@Slf4j
public class WorkoutBean implements Constants {

  @Autowired
  RavenBean ravenBean;

  IDocumentSession session;

  @Autowired
  private UserProvider userProvider;

  List<WorkoutYear> workoutYears;

  WorkoutYear selectedYear;

  /**
   * Workouts by year graph.
   */
  LineChartModel yearsGraph;

  @PostConstruct
  public void postConstruct() {
    log.info("WorkoutBean.postConstruct");

    log.info("userProvider = {}", userProvider);

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();
    log.info("SessionMap = {}", externalContext.getSessionMap());

    session = ravenBean.getSession();

    workoutYears = new LinkedList<>();

    yearsGraph = new LineChartModel();
    yearsGraph.setTitle("Swimming Distance per Year by Month");
    yearsGraph.setLegendPosition("n");
    yearsGraph.getAxes().put(AxisType.X, new CategoryAxis("Months"));

    Axis yAxis = yearsGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Miles");
    yAxis.setMin(0);

    List<Workout> workouts = session.query(Workout.class)
        .whereEquals("user", userProvider.getUser().getUsername())
        .whereEquals("workoutActivityType", SWIMMING_WORKOUT)
        .selectFields(Workout.class, "duration", "totalDistance",
            "startDate", "totalEnergyBurned")
        .toList();

    getActiveYears(workouts).stream().sorted().forEach(year -> {

      List<Workout> filtered = workouts.stream()
          .filter(workout -> workout.getStartDate().startsWith(year + "-"))
          .collect(Collectors.toList());

      WorkoutYear workoutYear = new WorkoutYear(year, filtered);

      workoutYears.add(workoutYear);

      LineChartSeries _year = new LineChartSeries();
      _year.setLabel(String.valueOf(year));

      double total = 0;
      for (int i = 0; i < 12; i++) {
        total += workoutYear.get(i + 1).getDistance();
        String month = new DateFormatSymbols().getMonths()[i];
        _year.set(month, total);
      }

      yearsGraph.addSeries(_year);

    });

    if (!workouts.isEmpty()) {
      selectedYear = workoutYears.get(0);
    }
  }

  @PreDestroy
  public void preDestroy() {
    session.close();
  }

  public List<WorkoutYear> getYears() {
    return workoutYears;
  }

  public void onRowSelect(SelectEvent<WorkoutYear> event) throws IOException {

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    String yearSelected = String.valueOf(event.getObject().getYear());
    log.info("yearSelected = {}", yearSelected);

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
    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Item selected",
        "Item Index: " + event.getItemIndex() + ", DataSet Index:" + event.getDataSetIndex());

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

    log.info("years = {}", years);
    return years;
  }

}
