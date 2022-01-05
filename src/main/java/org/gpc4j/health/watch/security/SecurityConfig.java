package org.gpc4j.health.watch.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Bean
  @Override
  public UserDetailService userDetailsService() {
    return new UserDetailService();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    log.info("Configuring security!");

    http.headers().httpStrictTransportSecurity().disable();

    http.csrf().disable();
    http.headers().frameOptions().disable();

    http.authorizeRequests()
        .antMatchers("/createaccount.xhtml").permitAll()
        .antMatchers("/javax.faces.resource/**").permitAll()
        .antMatchers("/createaccount.xhtml").hasRole("ADMIN")
        .antMatchers("/", "/month.xhtml").hasAnyRole("USER", "ADMIN")
        .antMatchers("/**").authenticated()
        .and()
        .formLogin()
        .successHandler(new LoginSuccessHandler())
        .and().logout()
        .logoutUrl("/logout");

  }

}
