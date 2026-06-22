package io.spring.common.exception;

public class InvalidAuthenticationException extends RuntimeException {
  public InvalidAuthenticationException() {
    super("invalid email or password");
  }
}
