package io.spring.article.api.exception;

public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException() {
    super("invalid email or password");
  }
}
