package org.gpc4j.health.watch.jsf.beans;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.db.dto.WorkoutDay;
import org.gpc4j.health.watch.security.UserProvider;
import org.gpc4j.health.watch.xml.Workout;
import org.gpc4j.health.watch.xml.WorkoutEvent;
import org.primefaces.component.chart.Chart;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.ChartDataSet;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.scatter.ScatterChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
  @Getter
  private int year;
  @Getter
  private int month;
  private int day;

  /**
   * Workouts for the day.
   */
  @Getter
  List<Workout> workouts;

  /**
   * Totals  for the day.
   */
  private double totalMiles;
  private double calories;

  /**
   * The Chart/Graph for laps.
   */
  @Getter
  LineChartModel segmentGraph;

  @Getter
  LineChartModel heartRateGraph;

  @Getter
  ScatterChartModel scatterChart;

  @PostConstruct
  public void postConstruct() {
    log.debug(this.toString());

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
                      .whereEquals("workoutActivityType",
                                   WORKOUT_MAP.get(cookieBean.getWorkout()))
                      .toList();

    totalMiles = workouts.stream()
                         .mapToDouble(Workout::getTotalDistance)
                         .sum();

    calories = workouts.stream()
                       .mapToDouble(Workout::getTotalEnergyBurned)
                       .sum();

    log.info("{}-{}-{} = {}", year, month, day, workouts.size());

    segmentGraph = new LineChartModel();
    segmentGraph.setTitle("Segments/Sets");
    segmentGraph.setLegendPosition("n");

    heartRateGraph = initHeartRateGraph();
    LineChartSeries heartRate = new LineChartSeries();
    heartRate.setLabel("Heart Rate");
    heartRateGraph.addSeries(heartRate);

    Axis yAxis = segmentGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Seconds per Length");
    yAxis.setTickInterval("10");

    Axis xAxis = segmentGraph.getAxis(AxisType.X);
    xAxis.setMin(0);
    xAxis.setLabel("Lengths");

    for (Workout workout : workouts) {
      int index = 1;
      int events = 0;  // Number of Events/Laps per Segment
      String segmentStartTime = null;
      String segmentDuration = null;
      LineChartSeries segment = null;
      for (WorkoutEvent event : workout.getWorkoutEvents()) {
        log.trace("event = {}", event);

        switch (event.getType()) {
          case PAUSE_WORKOUT:
          case RESUME_WORKOUT:
            log.debug("Skipping {}", event);
            continue;

          case SEGMENT:
            if (Objects.isNull(segment) || !segment.getData().isEmpty()) {
              log.debug("New Segment = {}", event);
              segmentStartTime = LocalDateTime
                  .parse(event.getDate(), DTF)
                  .format(DateTimeFormatter.ISO_TIME);
              segmentDuration = event.getDurationF();
              events = 0;
              segment = new LineChartSeries();
              segment.setShowMarker(false);
              segmentGraph.addSeries(segment);
            }
            break;

          default:
            events++;
            // TODO: Make event.duration a double
            segment.set(index, 60 * event.getDuration());
            segment.setLabel(segmentStartTime + "; "
                                 + events + "@"
                                 + segmentDuration);

            if (event.getHeartRate() > 0) {
              heartRate.set(index, event.getHeartRate());
            } else {
              heartRate.set(index, null);
            }
            index++;
            break;
        }

      }

    }

  }

  private LineChartModel initHeartRateGraph() {

    LineChartModel graph = new LineChartModel();
    graph.setTitle("Heart Rate");
    graph.setLegendPosition("n");

    Axis yAxis = graph.getAxis(AxisType.Y);
    yAxis.setLabel("Heart Rate");
    yAxis.setTickInterval("10");
    yAxis.setMin(80);
    yAxis.setMax(160);

    Axis xAxis = graph.getAxis(AxisType.X);
    xAxis.setMin(0);
    xAxis.setLabel("Lengths");

    return graph;
  }

  public void onRowSelect(SelectEvent<WorkoutDay> event) throws IOException {

    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    String monthSelected = String.valueOf(event.getObject().getDay());
    log.info("monthSelected = {}", monthSelected);

    externalContext.addResponseCookie(DAY_COOKIE_KEY, monthSelected, null);

    externalContext.redirect("day.xhtml");
  }

  public String getDate() {
    LocalDate date = LocalDate.of(year, month, day);
    return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                                        .withLocale(Locale.US));
  }

  public void heartRateSelect(ItemSelectEvent event) {

    log.debug("Item Index: {}, DataSet Index:{}",
              event.getItemIndex(), event.getDataSetIndex());

    Chart chart = (Chart) event.getComponent();
    LineChartModel model = (LineChartModel) chart.getModel();
    ChartSeries line = model.getSeries().get(event.getDataSetIndex());
    Map<Object, Number> data = line.getData();
    Short value = (Short) data.values().toArray()[event.getItemIndex()];

    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        data.keySet()
                                            .toArray()[event.getItemIndex()].toString() + ", " + line.getLabel(),
                                        String.format("%s Beats per minute", value));

    FacesContext.getCurrentInstance().addMessage(null, msg);
  }

}
