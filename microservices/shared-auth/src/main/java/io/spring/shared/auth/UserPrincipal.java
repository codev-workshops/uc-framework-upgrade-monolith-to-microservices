package io.spring.shared.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
  private String id;
  private String username;
  private String email;
}
