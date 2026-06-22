package io.spring.user.application;

import io.spring.user.core.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@UpdateUserConstraint
public class UpdateUserCommand {

  private User targetUser;
  private UpdateUserParam param;
}
