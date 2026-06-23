package io.spring.commentservice.security;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;
  private final UserServiceClient userServiceClient;
  private final String header = "Authorization";

  public JwtTokenFilter(JwtUtils jwtUtils, UserServiceClient userServiceClient) {
    this.jwtUtils = jwtUtils;
    this.userServiceClient = userServiceClient;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    JwtUtils.extractTokenFromHeader(request.getHeader(header))
        .flatMap(token -> jwtUtils.getSubFromToken(token))
        .ifPresent(
            userId -> {
              if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<UserData> userOpt = userServiceClient.getUserById(userId);
                userOpt.ifPresent(
                    userData -> {
                      UsernamePasswordAuthenticationToken authenticationToken =
                          new UsernamePasswordAuthenticationToken(
                              userData, null, Collections.emptyList());
                      authenticationToken.setDetails(
                          new WebAuthenticationDetailsSource().buildDetails(request));
                      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    });
              }
            });

    filterChain.doFilter(request, response);
  }
}
