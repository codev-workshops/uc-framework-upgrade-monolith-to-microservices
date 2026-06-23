package io.spring.shared.exception;

public class InvalidAuthenticationException extends RuntimeException {
  public InvalidAuthenticationException() {
    super("invalid email or password");
  }

  public InvalidAuthenticationException(String message) {
    super(message);
  }
}
