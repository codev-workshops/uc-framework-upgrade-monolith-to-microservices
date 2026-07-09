package io.spring.gateway.graphql.datafetchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import io.spring.gateway.graphql.datafetchers.QueryDatafetcher.NotFoundException;
import io.spring.gateway.graphql.support.GatewayClient;
import io.spring.gateway.graphql.support.GraphqlMappers;
import io.spring.gateway.graphql.types.ArticlePayload;
import io.spring.gateway.graphql.types.CommentPayload;
import io.spring.gateway.graphql.types.CreateArticleInput;
import io.spring.gateway.graphql.types.CreateUserInput;
import io.spring.gateway.graphql.types.DeletionStatus;
import io.spring.gateway.graphql.types.Error;
import io.spring.gateway.graphql.types.ErrorItem;
import io.spring.gateway.graphql.types.Profile;
import io.spring.gateway.graphql.types.ProfilePayload;
import io.spring.gateway.graphql.types.UpdateArticleInput;
import io.spring.gateway.graphql.types.UpdateUserInput;
import io.spring.gateway.graphql.types.User;
import io.spring.gateway.graphql.types.UserPayload;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/** GraphQL mutations, fanned out to the downstream services. */
@DgsComponent
public class MutationDatafetcher {

  private final GatewayClient client;

  public MutationDatafetcher(GatewayClient client) {
    this.client = client;
  }

  // ----- user & profile -----
  @DgsMutation(field = "createUser")
  public Object createUser(@InputArgument("input") CreateUserInput input) {
    ObjectNode user = client.mapper().createObjectNode();
    user.put("email", input.getEmail());
    user.put("username", input.getUsername());
    user.put("password", input.getPassword());
    ObjectNode body = client.mapper().createObjectNode();
    body.set("user", user);
    ResponseEntity<String> res =
        client.exchange(
            HttpMethod.POST, client.services().getUserAuth().getUrl() + "/users", body.toString());
    if (res.getStatusCode().value() == 422) {
      return toError(client.json(res));
    }
    return userPayload(client.json(res).path("user"));
  }

  @DgsMutation(field = "login")
  public UserPayload login(
      @InputArgument("password") String password, @InputArgument("email") String email) {
    ObjectNode user = client.mapper().createObjectNode();
    user.put("email", email);
    user.put("password", password);
    ObjectNode body = client.mapper().createObjectNode();
    body.set("user", user);
    ResponseEntity<String> res =
        client.exchange(
            HttpMethod.POST,
            client.services().getUserAuth().getUrl() + "/users/login",
            body.toString());
    if (!res.getStatusCode().is2xxSuccessful()) {
      throw new NotFoundException("invalid email or password");
    }
    return userPayload(client.json(res).path("user"));
  }

  @DgsMutation(field = "updateUser")
  public UserPayload updateUser(@InputArgument("changes") UpdateUserInput changes) {
    ObjectNode user = client.mapper().createObjectNode();
    putIfPresent(user, "email", changes.getEmail());
    putIfPresent(user, "username", changes.getUsername());
    putIfPresent(user, "password", changes.getPassword());
    putIfPresent(user, "image", changes.getImage());
    putIfPresent(user, "bio", changes.getBio());
    ObjectNode body = client.mapper().createObjectNode();
    body.set("user", user);
    ResponseEntity<String> res =
        client.exchange(
            HttpMethod.PUT, client.services().getUserAuth().getUrl() + "/user", body.toString());
    return userPayload(client.json(res).path("user"));
  }

  @DgsMutation(field = "followUser")
  public ProfilePayload followUser(@InputArgument("username") String username) {
    ResponseEntity<String> res =
        client.exchange(
            HttpMethod.POST,
            client.services().getProfile().getUrl() + "/profiles/" + username + "/follow",
            null);
    return ProfilePayload.newBuilder()
        .profile(GraphqlMappers.toProfile(client.json(res).path("profile")))
        .build();
  }

  @DgsMutation(field = "unfollowUser")
  public ProfilePayload unfollowUser(@InputArgument("username") String username) {
    ResponseEntity<String> res =
        client.exchange(
            HttpMethod.DELETE,
            client.services().getProfile().getUrl() + "/profiles/" + username + "/follow",
            null);
    return ProfilePayload.newBuilder()
        .profile(GraphqlMappers.toProfile(client.json(res).path("profile")))
        .build();
  }

  // ----- article -----
  @DgsMutation(field = "createArticle")
  public ArticlePayload createArticle(@InputArgument("input") CreateArticleInput input) {
    ObjectNode article = client.mapper().createObjectNode();
    article.put("title", input.getTitle());
    article.put("description", input.getDescription());
    article.put("body", input.getBody());
    ArrayNode tags = article.putArray("tagList");
    if (input.getTagList() != null) {
      input.getTagList().forEach(tags::add);
    }
    ObjectNode body = client.mapper().createObjectNode();
    body.set("article", article);
    ResponseEntity<String> res =
        client.exchange(HttpMethod.POST, client.articleUrl() + "/articles", body.toString());
    return ArticlePayload.newBuilder()
        .article(GraphqlMappers.toArticle(client.json(res).path("article")))
        .build();
  }

  @DgsMutation(field = "updateArticle")
  public ArticlePayload updateArticle(
      @InputArgument("slug") String slug, @InputArgument("changes") UpdateArticleInput changes) {
    ObjectNode article = client.mapper().createObjectNode();
    putIfPresent(article, "title", changes.getTitle());
    putIfPresent(article, "body", changes.getBody());
    putIfPresent(article, "description", changes.getDescription());
    ObjectNode body = client.mapper().createObjectNode();
    body.set("article", article);
    ResponseEntity<String> res =
        client.exchange(HttpMethod.PUT, client.articleUrl() + "/articles/" + slug, body.toString());
    if (!res.getStatusCode().is2xxSuccessful()) {
      throw new NotFoundException("cannot update article: " + slug);
    }
    return ArticlePayload.newBuilder()
        .article(GraphqlMappers.toArticle(client.json(res).path("article")))
        .build();
  }

  @DgsMutation(field = "favoriteArticle")
  public ArticlePayload favoriteArticle(@InputArgument("slug") String slug) {
    return mutateFavorite(slug, HttpMethod.POST);
  }

  @DgsMutation(field = "unfavoriteArticle")
  public ArticlePayload unfavoriteArticle(@InputArgument("slug") String slug) {
    return mutateFavorite(slug, HttpMethod.DELETE);
  }

  private ArticlePayload mutateFavorite(String slug, HttpMethod method) {
    String id =
        client
            .resolveArticleId(slug)
            .orElseThrow(() -> new NotFoundException("article not found: " + slug));
    client.exchange(
        method, client.services().getFavorite().getUrl() + "/articles/" + id + "/favorite", null);
    JsonNode article =
        client.articleById(id).orElseThrow(() -> new NotFoundException("article not found: " + id));
    return ArticlePayload.newBuilder().article(GraphqlMappers.toArticle(article)).build();
  }

  @DgsMutation(field = "deleteArticle")
  public DeletionStatus deleteArticle(@InputArgument("slug") String slug) {
    ResponseEntity<String> res =
        client.exchange(HttpMethod.DELETE, client.articleUrl() + "/articles/" + slug, null);
    return DeletionStatus.newBuilder().success(res.getStatusCode().is2xxSuccessful()).build();
  }

  // ----- comment -----
  @DgsMutation(field = "addComment")
  public CommentPayload addComment(
      @InputArgument("slug") String slug, @InputArgument("body") String bodyText) {
    ObjectNode comment = client.mapper().createObjectNode();
    comment.put("body", bodyText);
    ObjectNode body = client.mapper().createObjectNode();
    body.set("comment", comment);
    ResponseEntity<String> res =
        client.exchange(
            HttpMethod.POST,
            client.services().getComment().getUrl() + "/articles/" + slug + "/comments",
            body.toString());
    if (!res.getStatusCode().is2xxSuccessful()) {
      throw new NotFoundException("cannot add comment to: " + slug);
    }
    return CommentPayload.newBuilder()
        .comment(GraphqlMappers.toComment(client.json(res).path("comment")))
        .build();
  }

  @DgsMutation(field = "deleteComment")
  public DeletionStatus deleteComment(
      @InputArgument("slug") String slug, @InputArgument("id") String id) {
    ResponseEntity<String> res =
        client.exchange(
            HttpMethod.DELETE,
            client.services().getComment().getUrl() + "/articles/" + slug + "/comments/" + id,
            null);
    return DeletionStatus.newBuilder().success(res.getStatusCode().is2xxSuccessful()).build();
  }

  // ----- helpers -----
  private UserPayload userPayload(JsonNode user) {
    User result =
        User.newBuilder()
            .email(user.path("email").asText(null))
            .username(user.path("username").asText(null))
            .token(user.path("token").asText(null))
            .profile(
                Profile.newBuilder()
                    .username(user.path("username").asText(null))
                    .bio(user.path("bio").asText(null))
                    .image(user.path("image").asText(null))
                    .following(false)
                    .build())
            .build();
    return UserPayload.newBuilder().user(result).build();
  }

  private Error toError(JsonNode errorPayload) {
    JsonNode errors = errorPayload.path("errors");
    List<ErrorItem> items = new ArrayList<>();
    errors
        .fieldNames()
        .forEachRemaining(
            key -> {
              List<String> values = new ArrayList<>();
              errors.path(key).forEach(v -> values.add(v.asText()));
              items.add(ErrorItem.newBuilder().key(key).value(values).build());
            });
    return Error.newBuilder().message("validation failed").errors(items).build();
  }

  private void putIfPresent(ObjectNode node, String field, String value) {
    if (value != null) {
      node.put(field, value);
    }
  }
}
