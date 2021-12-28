package org.gpc4j.health.watch.jsf.beans;

import lombok.extern.slf4j.Slf4j;
import net.ravendb.client.documents.session.IDocumentSession;
import org.gpc4j.health.watch.db.RavenBean;
import org.gpc4j.health.watch.xml.Record;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import java.util.List;

@RequestScope
@Component("dayBean")
@Slf4j
public class DayBean {

  @Autowired
  RavenBean ravenBean;

  /**
   * The Chart/Graph object used by JSF/Primefaces.
   */
  LineChartModel dayGraph;

  @PostConstruct
  public void postConstruct() {
    dayGraph = new LineChartModel();
    dayGraph.setTitle("Lap Times");
    dayGraph.setLegendPosition("n");

    Axis xAxis = dayGraph.getAxis(AxisType.X);
    xAxis.setMin(-10);

    Axis yAxis = dayGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Seconds Per 25 yds");

    LineChartSeries set = new LineChartSeries();
    set.setShowMarker(false);
    set.setLabel("Swim");

    log.info("ravenBean = {}", ravenBean);

    IDocumentSession session = ravenBean.getSession();
    log.info("got session");

    List<Record> swimmingRecords = session.query(Record.class)
        .search("startDate", "2021-11-02*")
        .toList();

    log.info("swimmingRecords = {}", swimmingRecords.size());

    for (int i = 0; i < swimmingRecords.size(); i++) {
      set.set(i,swimmingRecords.get(i).getDuration());
    }

    dayGraph.addSeries(set);
  }

  public LineChartModel getDayGraph() {
    return dayGraph;
  }

}
