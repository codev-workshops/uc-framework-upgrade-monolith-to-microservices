package io.spring.userservice.api;

import io.spring.common.dto.UserData;
import io.spring.userservice.application.UserQueryService;
import io.spring.userservice.application.UserService;
import io.spring.userservice.application.data.UpdateUserCommand;
import io.spring.userservice.application.data.UpdateUserParam;
import io.spring.userservice.application.data.UserWithToken;
import io.spring.userservice.core.User;
import io.spring.userservice.core.UserRepository;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class CurrentUserApi {

  private UserQueryService userQueryService;
  private UserService userService;
  private UserRepository userRepository;

  @GetMapping
  public ResponseEntity currentUser(
      @AuthenticationPrincipal String userId,
      @RequestHeader(value = "Authorization") String authorization) {
    User currentUser = userRepository.findById(userId).orElseThrow(RuntimeException::new);
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return ResponseEntity.ok(
        userResponse(new UserWithToken(userData, authorization.split(" ")[1])));
  }

  @PutMapping
  public ResponseEntity updateProfile(
      @AuthenticationPrincipal String userId,
      @RequestHeader("Authorization") String token,
      @Valid @RequestBody UpdateUserParam updateUserParam) {
    User currentUser = userRepository.findById(userId).orElseThrow(RuntimeException::new);
    userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return ResponseEntity.ok(userResponse(new UserWithToken(userData, token.split(" ")[1])));
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}
