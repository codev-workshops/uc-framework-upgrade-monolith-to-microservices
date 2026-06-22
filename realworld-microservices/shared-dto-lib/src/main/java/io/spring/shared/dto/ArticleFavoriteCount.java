package io.spring.shared.dto;

import lombok.Value;

@Value
public class ArticleFavoriteCount {
    private String id;
    private Integer count;
}
