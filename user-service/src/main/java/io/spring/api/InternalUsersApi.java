package io.spring.api;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.contracts.dto.UserSummary;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inter-service endpoints backing the {@code UserServiceClient} contract, so other services can
 * resolve user projections without reading the {@code users} table.
 */
@RestController
@RequestMapping(path = "/internal/users")
@AllArgsConstructor
public class InternalUsersApi {

  private UserQueryService userQueryService;

  @GetMapping("/{id}")
  public ResponseEntity<UserSummary> findById(@PathVariable("id") String id) {
    return userQueryService
        .findById(id)
        .map(userData -> ResponseEntity.ok(toSummary(userData)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<UserSummary> findByUsername(@PathVariable("username") String username) {
    return userQueryService
        .findByUsername(username)
        .map(userData -> ResponseEntity.ok(toSummary(userData)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<UserSummary>> findByIds(@RequestParam("ids") List<String> ids) {
    return ResponseEntity.ok(
        userQueryService.findByIds(ids).stream()
            .map(InternalUsersApi::toSummary)
            .collect(Collectors.toList()));
  }

  private static UserSummary toSummary(UserData userData) {
    return new UserSummary(
        userData.getId(), userData.getUsername(), userData.getBio(), userData.getImage());
  }
}
