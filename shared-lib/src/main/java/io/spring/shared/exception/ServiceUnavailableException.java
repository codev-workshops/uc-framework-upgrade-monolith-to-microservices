package io.spring.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {
  public ServiceUnavailableException(String serviceName) {
    super(serviceName + " is currently unavailable");
  }

  public ServiceUnavailableException(String serviceName, Throwable cause) {
    super(serviceName + " is currently unavailable", cause);
  }
}
