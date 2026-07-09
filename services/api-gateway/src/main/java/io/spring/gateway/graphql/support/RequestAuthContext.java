package io.spring.gateway.graphql.support;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Holds the inbound Authorization header for the current request so GraphQL datafetchers and nested
 * field resolvers can forward it to the downstream services (mirroring the monolith's
 * SecurityContext-based current user).
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestAuthContext {
  private String authorization;

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  public boolean isAuthenticated() {
    return authorization != null && !authorization.isEmpty();
  }
}
