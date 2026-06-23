package io.spring.articleservice.api.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {
  private String id;
  private String username;
  private String email;
  private String bio;
  private String image;
}
