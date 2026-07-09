package io.spring.gateway.graphql.datafetchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
import io.spring.gateway.graphql.DgsConstants.ARTICLE;
import io.spring.gateway.graphql.support.GatewayClient;
import io.spring.gateway.graphql.support.GraphqlMappers;
import io.spring.gateway.graphql.support.Paging;
import io.spring.gateway.graphql.types.Article;
import io.spring.gateway.graphql.types.CommentsConnection;

/** Resolves the nested Article.comments connection via comment-service. */
@DgsComponent
public class CommentDatafetcher {

  private final GatewayClient client;

  public CommentDatafetcher(GatewayClient client) {
    this.client = client;
  }

  @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Comments)
  public CommentsConnection articleComments(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DataFetchingEnvironment dfe) {
    Article source = dfe.getSource();
    Paging p = Paging.from(first, after, last, before);
    JsonNode comments = client.listComments(source.getSlug());
    return GraphqlMappers.toCommentsConnection(comments, p.offset);
  }
}
