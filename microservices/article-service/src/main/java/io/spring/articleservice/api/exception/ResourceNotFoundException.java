package io.spring.articleservice.api.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException() {
    super("resource not found");
  }
}
