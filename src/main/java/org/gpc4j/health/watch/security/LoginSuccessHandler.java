package org.gpc4j.health.watch.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    log.info("LoginSuccessHandler.onAuthenticationSuccess2");
    send(response, "/", true);
  }

  private void send(HttpServletResponse response, String path, boolean redirect) throws IOException {
    if (redirect) {
      response.sendRedirect(path);
    } else {
      response.getWriter().write(path);
    }
  }

}
