package io.spring.userservice.api.security;

import io.spring.common.security.BaseWebSecurityConfig;
import io.spring.common.security.JwtTokenFilter;
import io.spring.userservice.domain.UserRepository;
import io.spring.userservice.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends BaseWebSecurityConfig {

  @Autowired private UserRepository userRepository;

  @Autowired private JwtService jwtService;

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  protected JwtTokenFilter jwtTokenFilter() {
    return new JwtTokenFilter(jwtSecret);
  }

  @Bean
  public UserServiceJwtTokenFilter userServiceJwtTokenFilter() {
    return new UserServiceJwtTokenFilter(userRepository, jwtService);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configureAuthorization(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .antMatchers(HttpMethod.POST, "/users", "/users/login")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/profiles/**")
        .permitAll()
        .antMatchers("/internal/**")
        .permitAll()
        .anyRequest()
        .authenticated();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.addFilterBefore(userServiceJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
  }
}
