package io.spring.application.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.contracts.client.FavoriteServiceClient;
import io.spring.contracts.client.ProfileServiceClient;
import io.spring.contracts.client.TagServiceClient;
import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.dto.UserSummary;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Verifies the API read composition that replaces the monolith's in-process joins: tags, favorite
 * count/state and author follow state are stitched together from the stubbed service clients.
 */
public class ArticleQueryServiceTest {

  private ArticleReadService articleReadService;
  private UserServiceClient userServiceClient;
  private TagServiceClient tagServiceClient;
  private FavoriteServiceClient favoriteServiceClient;
  private ProfileServiceClient profileServiceClient;
  private ArticleQueryService articleQueryService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleReadService = Mockito.mock(ArticleReadService.class);
    userServiceClient = Mockito.mock(UserServiceClient.class);
    tagServiceClient = Mockito.mock(TagServiceClient.class);
    favoriteServiceClient = Mockito.mock(FavoriteServiceClient.class);
    profileServiceClient = Mockito.mock(ProfileServiceClient.class);
    articleQueryService =
        new ArticleQueryService(
            articleReadService,
            userServiceClient,
            tagServiceClient,
            favoriteServiceClient,
            profileServiceClient);
  }

  private ArticleData baseArticle(String id, String authorId) {
    ArticleData data = new ArticleData();
    data.setId(id);
    data.setSlug("slug-" + id);
    data.setTitle("title");
    data.setProfileData(new ProfileData(authorId, null, null, null, false));
    return data;
  }

  @Test
  public void should_compose_single_article_for_current_user() {
    ArticleData base = baseArticle("a1", "author1");
    when(articleReadService.findBySlug("slug-a1")).thenReturn(base);
    when(tagServiceClient.tagsOfArticles(anyList())).thenReturn(Map.of("a1", List.of("java")));
    when(userServiceClient.findByIds(anyList()))
        .thenReturn(List.of(new UserSummary("author1", "john", "bio", "img")));
    when(profileServiceClient.followingAuthors(eq("me"), anyList())).thenReturn(Set.of("author1"));
    when(favoriteServiceClient.isFavorited("me", "a1")).thenReturn(true);
    when(favoriteServiceClient.articleFavoriteCount("a1")).thenReturn(3);

    Optional<ArticleData> result = articleQueryService.findBySlug("slug-a1", "me");

    assertThat(result).isPresent();
    ArticleData article = result.get();
    assertThat(article.getTagList()).containsExactly("java");
    assertThat(article.isFavorited()).isTrue();
    assertThat(article.getFavoritesCount()).isEqualTo(3);
    assertThat(article.getProfileData().getUsername()).isEqualTo("john");
    assertThat(article.getProfileData().isFollowing()).isTrue();
  }

  @Test
  public void should_return_empty_for_missing_slug() {
    when(articleReadService.findBySlug(any())).thenReturn(null);
    assertThat(articleQueryService.findBySlug("nope", "me")).isEmpty();
  }

  @Test
  public void should_list_and_filter_by_tag_with_favorite_counts() {
    ArticleData a1 = baseArticle("a1", "author1");
    ArticleData a2 = baseArticle("a2", "author2");
    when(articleReadService.findAll()).thenReturn(List.of(a1, a2));
    when(tagServiceClient.tagsOfArticles(anyList()))
        .thenReturn(Map.of("a1", List.of("java"), "a2", List.of("go")));
    when(userServiceClient.findByIds(anyList()))
        .thenReturn(List.of(new UserSummary("author1", "john", "bio", "img")));
    when(favoriteServiceClient.favoriteCounts(anyList())).thenReturn(Map.of("a1", 5));

    ArticleDataList list =
        articleQueryService.findRecentArticles("java", null, null, new Page(0, 20), null);

    assertThat(list.getCount()).isEqualTo(1);
    assertThat(list.getArticleDatas()).hasSize(1);
    ArticleData only = list.getArticleDatas().get(0);
    assertThat(only.getId()).isEqualTo("a1");
    assertThat(only.getFavoritesCount()).isEqualTo(5);
    assertThat(only.getTagList()).containsExactly("java");
  }
}
