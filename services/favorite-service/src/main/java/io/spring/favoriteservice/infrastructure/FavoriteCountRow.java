package io.spring.favoriteservice.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCountRow {
  private String id;
  private int favoriteCount;
}
