package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ArticleFavoriteCount {
  String id;
  Integer count;
}
