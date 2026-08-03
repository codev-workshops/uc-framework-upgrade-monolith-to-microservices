package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.contracts.dto.ProfileData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * RealWorld comment payload: {@code userId} is the referenced author id, resolved by composition.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData {
  private String id;
  private String body;
  @JsonIgnore private String articleId;
  @JsonIgnore private String userId;
  private DateTime createdAt;
  private DateTime updatedAt;

  @JsonProperty("author")
  private ProfileData profileData;
}
