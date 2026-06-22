package io.spring.userservice.application.data;

import io.spring.userservice.core.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateUserCommand {
  private User targetUser;
  private UpdateUserParam param;
}
