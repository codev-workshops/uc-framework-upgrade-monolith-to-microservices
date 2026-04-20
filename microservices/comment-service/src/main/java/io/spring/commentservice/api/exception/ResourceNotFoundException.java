package io.spring.commentservice.api.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException() {
    super("resource not found");
  }
}
