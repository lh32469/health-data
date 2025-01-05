package org.gpc4j.health.watch.jsf.beans;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.Map;

@RequestScope
@Component
@Slf4j
@Data
public class CookieBean implements Constants {

  /**
   * Get Year, Month and Day from Cookies passed in.
   */
  private int year;
  private int month;
  private int day;
  private String workout;

  @PostConstruct
  public void postConstruct() {
    log.debug("CookieBean.postConstruct");
    ExternalContext externalContext =
        FacesContext.getCurrentInstance().getExternalContext();

    Map<String, Object> cookieMap = externalContext.getRequestCookieMap();

    Cookie yearCookie = (Cookie) cookieMap.get(YEAR_COOKIE_KEY);

    if (null == yearCookie) {
      year = LocalDate.now().getYear();
    } else {
      year = Integer.parseInt(yearCookie.getValue());
    }

    Cookie monthCookie = (Cookie) cookieMap.get(MONTH_COOKIE_KEY);

    if (null == monthCookie) {
      month = LocalDate.now().getMonthValue();
    } else {
      month = Integer.parseInt(monthCookie.getValue());
    }

    Cookie dayCookie = (Cookie) cookieMap.get(DAY_COOKIE_KEY);
    if (null == dayCookie) {
      day = LocalDate.now().getDayOfMonth();
    } else {
      day = Integer.parseInt(dayCookie.getValue());
    }

    Cookie workoutCookie = (Cookie) externalContext
        .getRequestCookieMap()
        .get(WORKOUT_COOKIE_KEY);

    if (null == workoutCookie) {
      workout = WORKOUT_MAP.keySet().toArray()[0].toString();
    } else {
      workout = workoutCookie.getValue();
    }

    log.debug("year, month, day = {}, {}, {}", year, month, day);
    log.debug("workout = {}", workout);

  }

}
