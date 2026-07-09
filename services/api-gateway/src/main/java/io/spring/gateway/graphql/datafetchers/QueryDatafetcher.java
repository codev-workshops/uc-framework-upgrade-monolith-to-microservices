package io.spring.gateway.graphql.datafetchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.gateway.graphql.DgsConstants.PROFILE;
import io.spring.gateway.graphql.DgsConstants.QUERY;
import io.spring.gateway.graphql.support.GatewayClient;
import io.spring.gateway.graphql.support.GraphqlMappers;
import io.spring.gateway.graphql.support.Paging;
import io.spring.gateway.graphql.types.Article;
import io.spring.gateway.graphql.types.ArticlesConnection;
import io.spring.gateway.graphql.types.Profile;
import io.spring.gateway.graphql.types.ProfilePayload;
import io.spring.gateway.graphql.types.User;
import java.util.List;

/** Top-level GraphQL queries, fanned out to the downstream services. */
@DgsComponent
public class QueryDatafetcher {

  private final GatewayClient client;

  public QueryDatafetcher(GatewayClient client) {
    this.client = client;
  }

  @DgsQuery(field = QUERY.Article)
  public Article article(@InputArgument("slug") String slug) {
    JsonNode a =
        client
            .articleBySlug(slug)
            .orElseThrow(() -> new NotFoundException("article not found: " + slug));
    return GraphqlMappers.toArticle(a);
  }

  @DgsQuery(field = QUERY.Articles)
  public ArticlesConnection articles(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      @InputArgument("authoredBy") String authoredBy,
      @InputArgument("favoritedBy") String favoritedBy,
      @InputArgument("withTag") String withTag) {
    Paging p = Paging.from(first, after, last, before);
    JsonNode list = client.listArticles(p.offset, p.limit, withTag, authoredBy, favoritedBy, false);
    return GraphqlMappers.toArticlesConnection(list, p.offset);
  }

  @DgsQuery(field = QUERY.Feed)
  public ArticlesConnection feed(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before) {
    Paging p = Paging.from(first, after, last, before);
    JsonNode list = client.listArticles(p.offset, p.limit, null, null, null, true);
    return GraphqlMappers.toArticlesConnection(list, p.offset);
  }

  @DgsQuery(field = QUERY.Me)
  public DataFetcherResult<User> me() {
    if (!client.auth().isAuthenticated()) {
      return null;
    }
    JsonNode res = client.getJson(client.services().getUserAuth().getUrl() + "/user");
    JsonNode user = res.path("user");
    if (user.isMissingNode() || user.path("username").isMissingNode()) {
      return null;
    }
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
    return DataFetcherResult.<User>newResult().data(result).build();
  }

  @DgsQuery(field = QUERY.Profile)
  public ProfilePayload profile(@InputArgument("username") String username) {
    JsonNode p =
        client
            .profile(username)
            .orElseThrow(() -> new NotFoundException("profile not found: " + username));
    return ProfilePayload.newBuilder().profile(GraphqlMappers.toProfile(p)).build();
  }

  @DgsQuery(field = QUERY.Tags)
  public List<String> tags() {
    return client.tags();
  }

  // ----- Profile nested connections -----
  @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Articles)
  public ArticlesConnection profileArticles(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      graphql.schema.DataFetchingEnvironment dfe) {
    Profile source = dfe.getSource();
    Paging p = Paging.from(first, after, last, before);
    JsonNode list = client.listArticles(p.offset, p.limit, null, source.getUsername(), null, false);
    return GraphqlMappers.toArticlesConnection(list, p.offset);
  }

  @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Favorites)
  public ArticlesConnection profileFavorites(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      graphql.schema.DataFetchingEnvironment dfe) {
    Profile source = dfe.getSource();
    Paging p = Paging.from(first, after, last, before);
    JsonNode list = client.listArticles(p.offset, p.limit, null, null, source.getUsername(), false);
    return GraphqlMappers.toArticlesConnection(list, p.offset);
  }

  @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Feed)
  public ArticlesConnection profileFeed(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before) {
    // The public API exposes the feed only for the authenticated current user.
    Paging p = Paging.from(first, after, last, before);
    JsonNode list = client.listArticles(p.offset, p.limit, null, null, null, true);
    return GraphqlMappers.toArticlesConnection(list, p.offset);
  }

  /** Surfaced as a GraphQL error by the DGS default exception handler. */
  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
      super(message);
    }
  }
}
