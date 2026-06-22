package io.spring.userservice.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.common.dto.UserData;
import io.spring.userservice.api.exception.InvalidAuthenticationException;
import io.spring.userservice.application.UserQueryService;
import io.spring.userservice.application.UserService;
import io.spring.userservice.application.data.RegisterParam;
import io.spring.userservice.application.data.UserWithToken;
import io.spring.userservice.core.User;
import io.spring.userservice.core.UserRepository;
import io.spring.userservice.infrastructure.service.DefaultJwtService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UsersApi {
  private UserRepository userRepository;
  private UserQueryService userQueryService;
  private PasswordEncoder passwordEncoder;
  private DefaultJwtService jwtService;
  private UserService userService;

  @PostMapping("/users")
  public ResponseEntity createUser(@Valid @RequestBody RegisterParam registerParam) {
    User user = userService.createUser(registerParam);
    UserData userData = userQueryService.findById(user.getId()).get();
    return ResponseEntity.status(201)
        .body(userResponse(new UserWithToken(userData, jwtService.toToken(user))));
  }

  @PostMapping("/users/login")
  public ResponseEntity userLogin(@Valid @RequestBody LoginParam loginParam) {
    return userRepository
        .findByEmail(loginParam.getEmail())
        .filter(user -> passwordEncoder.matches(loginParam.getPassword(), user.getPassword()))
        .map(
            user -> {
              UserData userData = userQueryService.findById(user.getId()).get();
              return ResponseEntity.ok(
                  userResponse(new UserWithToken(userData, jwtService.toToken(user))));
            })
        .orElseThrow(InvalidAuthenticationException::new);
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String password;
}
