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
        .antMatchers( "/favicon.ico").permitAll()
        .antMatchers("/actuator/health").permitAll()
        .antMatchers("/actuator/prometheus").hasIpAddress("192.0.0.0/8")
        .antMatchers("/actuator/prometheus").hasIpAddress("10.0.0.0/8")
        .antMatchers("/actuator").authenticated()
        .antMatchers("/actuator/**").authenticated()
        .antMatchers("/instances").authenticated()
        .antMatchers("/instances/**").authenticated()
        .and()
        .httpBasic();

    http.authorizeRequests()
        .antMatchers("/createaccount.xhtml").permitAll()
        .antMatchers("/javax.faces.resource/**").permitAll()
        .antMatchers("/createaccount.xhtml").hasRole("ADMIN")
        .antMatchers("/", "/month.xhtml").hasAnyRole("USER", "ADMIN")
        .antMatchers("/**/*.xhtml").hasAnyRole("USER", "ADMIN")
        .antMatchers("/**").authenticated()
        .and()
        .formLogin()
        .successHandler(new LoginSuccessHandler())
        .and().logout()
        .logoutUrl("/logout")
        .and()
//        .csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//            .ignoringRequestMatchers(
//                new AntPathRequestMatcher(("/instances"),
//                    HttpMethod.POST.toString()),
//                new AntPathRequestMatcher(("/instances/*"),
//                    HttpMethod.DELETE.toString()),
//                new AntPathRequestMatcher(("/actuator/**"))
//            ))
        .rememberMe().key("3f27f2b04fc6");

  }

}
