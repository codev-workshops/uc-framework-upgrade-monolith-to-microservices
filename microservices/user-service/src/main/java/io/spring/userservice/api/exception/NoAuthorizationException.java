package io.spring.userservice.api.exception;

public class NoAuthorizationException extends RuntimeException {
  public NoAuthorizationException() {
    super("no authorization");
  }
}
