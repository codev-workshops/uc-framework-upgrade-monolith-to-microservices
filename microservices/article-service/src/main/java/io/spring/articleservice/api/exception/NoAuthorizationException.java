package io.spring.articleservice.api.exception;

public class NoAuthorizationException extends RuntimeException {
  public NoAuthorizationException() {
    super("no authorization");
  }
}
