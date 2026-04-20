package io.spring.commentservice.api.exception;

public class NoAuthorizationException extends RuntimeException {
  public NoAuthorizationException() {
    super("no authorization");
  }
}
