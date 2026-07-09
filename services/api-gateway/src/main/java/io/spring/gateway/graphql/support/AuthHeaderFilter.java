package io.spring.gateway.graphql.support;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Captures the Authorization header into the request-scoped {@link RequestAuthContext}. */
@Component
public class AuthHeaderFilter extends OncePerRequestFilter {

  private final ObjectFactory<RequestAuthContext> contextFactory;

  public AuthHeaderFilter(ObjectFactory<RequestAuthContext> contextFactory) {
    this.contextFactory = contextFactory;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null) {
      contextFactory.getObject().setAuthorization(authorization);
    }
    filterChain.doFilter(request, response);
  }
}
