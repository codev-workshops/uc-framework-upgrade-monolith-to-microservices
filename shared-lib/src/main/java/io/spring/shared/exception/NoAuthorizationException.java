package io.spring.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NoAuthorizationException extends RuntimeException {
  public NoAuthorizationException() {
    super();
  }

  public NoAuthorizationException(String message) {
    super(message);
  }
}
