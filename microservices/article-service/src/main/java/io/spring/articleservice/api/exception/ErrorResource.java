package io.spring.articleservice.api.exception;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import lombok.Getter;

@JsonRootName("errors")
@Getter
public class ErrorResource {
  private List<FieldErrorResource> fieldErrors;

  public ErrorResource(List<FieldErrorResource> fieldErrors) {
    this.fieldErrors = fieldErrors;
  }
}
