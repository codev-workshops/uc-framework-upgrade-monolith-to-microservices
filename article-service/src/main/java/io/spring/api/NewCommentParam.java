package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Public RealWorld comment write payload ({@code {"comment": {"body": ...}}}). */
@Getter
@NoArgsConstructor
@JsonRootName("comment")
public class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
