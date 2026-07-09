package io.spring.gateway.graphql.support;

import com.fasterxml.jackson.databind.JsonNode;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import io.spring.gateway.graphql.types.Article;
import io.spring.gateway.graphql.types.ArticleEdge;
import io.spring.gateway.graphql.types.ArticlesConnection;
import io.spring.gateway.graphql.types.Comment;
import io.spring.gateway.graphql.types.CommentEdge;
import io.spring.gateway.graphql.types.CommentsConnection;
import io.spring.gateway.graphql.types.Profile;
import java.util.ArrayList;
import java.util.List;

/** Builds DGS-generated GraphQL types from the composed JSON returned by the services. */
public final class GraphqlMappers {

  private GraphqlMappers() {}

  public static Profile toProfile(JsonNode n) {
    return Profile.newBuilder()
        .username(text(n, "username"))
        .bio(text(n, "bio"))
        .image(text(n, "image"))
        .following(n.path("following").asBoolean(false))
        .build();
  }

  public static Article toArticle(JsonNode a) {
    List<String> tagList = new ArrayList<>();
    a.path("tagList").forEach(t -> tagList.add(t.asText()));
    return Article.newBuilder()
        .slug(text(a, "slug"))
        .title(text(a, "title"))
        .description(text(a, "description"))
        .body(text(a, "body"))
        .tagList(tagList)
        .createdAt(text(a, "createdAt"))
        .updatedAt(text(a, "updatedAt"))
        .favorited(a.path("favorited").asBoolean(false))
        .favoritesCount(a.path("favoritesCount").asInt(0))
        .author(toProfile(a.path("author")))
        .build();
  }

  public static Comment toComment(JsonNode c) {
    return Comment.newBuilder()
        .id(text(c, "id"))
        .body(text(c, "body"))
        .createdAt(text(c, "createdAt"))
        .updatedAt(text(c, "updatedAt"))
        .author(toProfile(c.path("author")))
        .build();
  }

  /**
   * Builds a Relay connection over an offset-paginated article list. Cursors are the absolute
   * offset index (the underlying REST list endpoints use offset/limit rather than the monolith's
   * date cursors — see the migration README).
   */
  public static ArticlesConnection toArticlesConnection(JsonNode list, int offset) {
    JsonNode articles = list.path("articles");
    int total = list.path("articlesCount").asInt(0);
    List<ArticleEdge> edges = new ArrayList<>();
    int i = 0;
    for (JsonNode a : articles) {
      edges.add(
          ArticleEdge.newBuilder().cursor(String.valueOf(offset + i)).node(toArticle(a)).build());
      i++;
    }
    return ArticlesConnection.newBuilder()
        .edges(edges)
        .pageInfo(pageInfo(offset, i, total))
        .build();
  }

  public static CommentsConnection toCommentsConnection(JsonNode comments, int offset) {
    List<CommentEdge> edges = new ArrayList<>();
    int i = 0;
    for (JsonNode c : comments) {
      edges.add(
          CommentEdge.newBuilder().cursor(String.valueOf(offset + i)).node(toComment(c)).build());
      i++;
    }
    int total = offset + i;
    return CommentsConnection.newBuilder()
        .edges(edges)
        .pageInfo(pageInfo(offset, i, total))
        .build();
  }

  private static DefaultPageInfo pageInfo(int offset, int size, int total) {
    return new DefaultPageInfo(
        size == 0 ? null : new DefaultConnectionCursor(String.valueOf(offset)),
        size == 0 ? null : new DefaultConnectionCursor(String.valueOf(offset + size - 1)),
        offset > 0,
        offset + size < total);
  }

  private static String text(JsonNode n, String field) {
    JsonNode v = n.path(field);
    return v.isMissingNode() || v.isNull() ? null : v.asText();
  }
}
