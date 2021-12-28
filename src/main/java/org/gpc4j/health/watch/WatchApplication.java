package org.gpc4j.health.watch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class WatchApplication {

  public static void main(String[] args) {
    SpringApplication.run(WatchApplication.class, args);
  }

  @GetMapping("/")
  public String homePage(Model model) {
    return "/workouts.xhtml";
  }

}
