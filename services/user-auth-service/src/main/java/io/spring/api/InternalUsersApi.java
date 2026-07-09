package io.spring.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.AuthorRef;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints (NOT exposed by the gateway). They return the {@link AuthorRef}
 * author projection that non-user services embed instead of joining the {@code users} table.
 */
@RestController
@RequestMapping(path = "/internal/users")
@AllArgsConstructor
public class InternalUsersApi {

  private UserQueryService userQueryService;

  // The service configures spring.jackson.deserialization.UNWRAP_ROOT_VALUE=true (needed to unwrap
  // the {"user":{...}} request bodies), which is incompatible with binding a top-level JSON array.
  // The batch endpoint therefore parses its raw array body with a dedicated non-unwrapping mapper.
  private static final ObjectMapper BATCH_MAPPER =
      new ObjectMapper().disable(DeserializationFeature.UNWRAP_ROOT_VALUE);

  @GetMapping("/{id}")
  public ResponseEntity<AuthorRef> getById(@PathVariable("id") String id) {
    return ResponseEntity.ok(
        userQueryService
            .findById(id)
            .map(AuthorRef::fromUserData)
            .orElseThrow(ResourceNotFoundException::new));
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<AuthorRef> getByUsername(@PathVariable("username") String username) {
    return ResponseEntity.ok(
        userQueryService
            .findByUsername(username)
            .map(AuthorRef::fromUserData)
            .orElseThrow(ResourceNotFoundException::new));
  }

  @PostMapping("/batch")
  public ResponseEntity<List<AuthorRef>> batch(@RequestBody String body) {
    final List<String> ids;
    try {
      ids = BATCH_MAPPER.readValue(body, new TypeReference<List<String>>() {});
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid id array");
    }
    List<AuthorRef> refs =
        ids.stream()
            .map(id -> userQueryService.findById(id).orElse(null))
            .filter(userData -> userData != null)
            .map(AuthorRef::fromUserData)
            .collect(Collectors.toList());
    return ResponseEntity.ok(refs);
  }
}
