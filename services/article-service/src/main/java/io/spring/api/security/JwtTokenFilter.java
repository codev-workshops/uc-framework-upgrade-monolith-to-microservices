package io.spring.api.security;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the JWT locally (shared signing key) and, on success, sets a lightweight {@link User}
 * principal whose id is the token {@code sub}. No network hop or local users table is required.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class JwtTokenFilter extends OncePerRequestFilter {
  @Autowired private JwtService jwtService;
  private final String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    getTokenString(request.getHeader(header))
        .flatMap(token -> jwtService.getSubFromToken(token))
        .ifPresent(
            id -> {
              if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = new User(id);
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
              }
            });

    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenString(String header) {
    if (header == null) {
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
