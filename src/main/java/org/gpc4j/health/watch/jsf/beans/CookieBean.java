package org.gpc4j.health.watch.jsf.beans;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import java.time.LocalDate;

@RequestScope
@Component
@Slf4j
@Data
public class CookieBean implements Constants {

  /**
   * Get Year, Month and Day from Cookies passed in.
   */
  private String workout;

  @Value("#{cookie[" + YEAR_COOKIE_KEY + "]?.value ?: 0}")
  private int year;

  @Value("#{cookie[" + MONTH_COOKIE_KEY + "]?.value ?: 0}")
  private int month;

  @Value("#{cookie[" + DAY_COOKIE_KEY + "]?.value ?: 0}")
  private int day;

  @Value("#{cookie[" + WORKOUT_COOKIE_KEY + "]}")
  private Cookie workoutCookie;

  @PostConstruct
  public void postConstruct() {
    log.debug("CookieBean.postConstruct");

    if (year == 0) {
      year = LocalDate.now().getYear();
    }

    if (month == 0) {
      month = LocalDate.now().getMonthValue();
    }

    if (day == 0) {
      day = LocalDate.now().getDayOfMonth();
    }

    if (null == workoutCookie) {
      workout = WORKOUT_MAP.keySet().toArray()[0].toString();
    } else {
      workout = workoutCookie.getValue();
    }

    log.info("year, month, day = {}, {}, {}", year, month, day);
    log.info("workout = {}", workout);

  }

}
