package io.spring.userservice.service;

import io.spring.userservice.domain.User;
import io.spring.userservice.domain.UserRepository;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class UserService {
  private UserRepository userRepository;
  private String defaultImage;
  private PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(
      UserRepository userRepository,
      @Value("${image.default}") String defaultImage,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.defaultImage = defaultImage;
    this.passwordEncoder = passwordEncoder;
  }

  public User createUser(String email, String username, String password) {
    User user =
        new User(email, username, passwordEncoder.encode(password), "", defaultImage);
    userRepository.save(user);
    return user;
  }

  public void updateUser(User user, String email, String username, String password,
      String bio, String image) {
    user.update(email, username, password, bio, image);
    userRepository.save(user);
  }
}
