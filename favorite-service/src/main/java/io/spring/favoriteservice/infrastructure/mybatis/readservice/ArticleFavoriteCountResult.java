package io.spring.favoriteservice.infrastructure.mybatis.readservice;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArticleFavoriteCountResult {
  private String id;
  private int favoriteCount;
}
