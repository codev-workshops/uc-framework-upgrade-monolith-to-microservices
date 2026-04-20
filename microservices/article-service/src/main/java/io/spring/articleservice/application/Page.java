package io.spring.articleservice.application;

import lombok.Getter;

@Getter
public class Page {
  private static final int MAX_LIMIT = 100;
  private int offset;
  private int limit;

  public Page(int offset, int limit) {
    this.offset = offset;
    this.limit = Math.min(limit, MAX_LIMIT);
  }
}
