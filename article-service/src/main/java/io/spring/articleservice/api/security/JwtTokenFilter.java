package io.spring.articleservice.api.security;

import io.spring.shared.client.UserServiceClient;
import io.spring.shared.dto.UserData;
import io.spring.shared.security.JwtUtils;
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

public class JwtTokenFilter extends OncePerRequestFilter {
  @Autowired private JwtUtils jwtUtils;
  @Autowired private UserServiceClient userServiceClient;
  private final String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    JwtUtils.extractTokenFromHeader(request.getHeader(header))
        .flatMap(token -> jwtUtils.getSubFromToken(token))
        .ifPresent(
            id -> {
              if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<UserData> userData = userServiceClient.getUserById(id);
                userData.ifPresent(
                    u -> {
                      AuthenticatedUser authenticatedUser =
                          new AuthenticatedUser(
                              u.getId(), u.getUsername(), u.getEmail(), u.getBio(), u.getImage());
                      UsernamePasswordAuthenticationToken authenticationToken =
                          new UsernamePasswordAuthenticationToken(
                              authenticatedUser, null, Collections.emptyList());
                      authenticationToken.setDetails(
                          new WebAuthenticationDetailsSource().buildDetails(request));
                      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    });
              }
            });

    filterChain.doFilter(request, response);
  }
}
