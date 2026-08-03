package io.spring.api;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Write payload for an internal comment creation, keyed by referenced {@code userId}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String userId;
}
