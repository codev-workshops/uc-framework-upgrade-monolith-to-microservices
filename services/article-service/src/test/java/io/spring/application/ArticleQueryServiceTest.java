package io.spring.application;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.client.FavoriteClient;
import io.spring.client.ProfileClient;
import io.spring.client.UserAuthClient;
import io.spring.client.dto.AuthorRef;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import java.util.ArrayList;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleQueryServiceTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private UserAuthClient userAuthClient;
  @Mock private FavoriteClient favoriteClient;
  @Mock private ProfileClient profileClient;

  private ArticleQueryService service;

  @BeforeEach
  void setUp() {
    service =
        new ArticleQueryService(articleReadService, userAuthClient, favoriteClient, profileClient);
  }

  private ArticleData baseArticle(String id, String authorId) {
    ProfileData profile = new ProfileData();
    profile.setId(authorId);
    return new ArticleData(
        id,
        id + "-slug",
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        new ArrayList<>(singletonList("java")),
        profile);
  }

  @Test
  void findBySlug_composesAuthorFavoritesAndFollowing() {
    ArticleData article = baseArticle("a1", "user-1");
    when(articleReadService.findBySlug("a1-slug")).thenReturn(article);
    when(userAuthClient.findByIds(singletonList("user-1")))
        .thenReturn(singletonList(new AuthorRef("user-1", "jake", "bio", "img")));
    when(favoriteClient.countsForArticles(singletonList("a1")))
        .thenReturn(singletonList(new ArticleFavoriteCount("a1", 5)));
    when(favoriteClient.favoritedArticles("me", singletonList("a1")))
        .thenReturn(singletonList("a1"));
    when(profileClient.followingAmong("me", singletonList("user-1")))
        .thenReturn(singletonList("user-1"));

    Optional<ArticleData> result = service.findBySlug("a1-slug", new User("me"));

    assertThat(result).isPresent();
    ArticleData data = result.get();
    assertThat(data.getProfileData().getUsername()).isEqualTo("jake");
    assertThat(data.getProfileData().getBio()).isEqualTo("bio");
    assertThat(data.getFavoritesCount()).isEqualTo(5);
    assertThat(data.isFavorited()).isTrue();
    assertThat(data.getProfileData().isFollowing()).isTrue();
  }

  @Test
  void findBySlug_anonymous_skipsUserSpecificCalls() {
    ArticleData article = baseArticle("a1", "user-1");
    when(articleReadService.findBySlug("a1-slug")).thenReturn(article);
    when(userAuthClient.findByIds(singletonList("user-1")))
        .thenReturn(singletonList(new AuthorRef("user-1", "jake", "bio", "img")));
    when(favoriteClient.countsForArticles(singletonList("a1")))
        .thenReturn(singletonList(new ArticleFavoriteCount("a1", 2)));

    Optional<ArticleData> result = service.findBySlug("a1-slug", null);

    assertThat(result).isPresent();
    assertThat(result.get().getFavoritesCount()).isEqualTo(2);
    assertThat(result.get().isFavorited()).isFalse();
    assertThat(result.get().getProfileData().isFollowing()).isFalse();
    verify(favoriteClient, never()).favoritedArticles(any(), any());
    verify(profileClient, never()).followingAmong(any(), any());
  }

  @Test
  void findBySlug_missing_returnsEmpty() {
    when(articleReadService.findBySlug("nope")).thenReturn(null);
    assertThat(service.findBySlug("nope", null)).isEmpty();
  }

  @Test
  void authorFilter_resolvesUsernameToId() {
    when(userAuthClient.findByUsername("jake"))
        .thenReturn(Optional.of(new AuthorRef("user-1", "jake", "", "")));
    when(articleReadService.queryArticles(eq(null), eq("user-1"), eq(null), any()))
        .thenReturn(singletonList("a1"));
    when(articleReadService.countArticle(eq(null), eq("user-1"), eq(null))).thenReturn(1);
    ArticleData article = baseArticle("a1", "user-1");
    when(articleReadService.findArticles(singletonList("a1")))
        .thenReturn(new ArrayList<>(singletonList(article)));
    when(userAuthClient.findByIds(singletonList("user-1")))
        .thenReturn(singletonList(new AuthorRef("user-1", "jake", "", "")));
    when(favoriteClient.countsForArticles(singletonList("a1")))
        .thenReturn(singletonList(new ArticleFavoriteCount("a1", 0)));

    ArticleDataList list = service.findRecentArticles(null, "jake", null, new Page(0, 20), null);

    assertThat(list.getCount()).isEqualTo(1);
    assertThat(list.getArticleDatas()).hasSize(1);
    verify(articleReadService).queryArticles(eq(null), eq("user-1"), eq(null), any());
  }

  @Test
  void authorFilter_unknownUsername_returnsEmpty() {
    when(userAuthClient.findByUsername("ghost")).thenReturn(Optional.empty());
    ArticleDataList list = service.findRecentArticles(null, "ghost", null, new Page(0, 20), null);
    assertThat(list.getCount()).isZero();
    assertThat(list.getArticleDatas()).isEmpty();
    verify(articleReadService, never()).queryArticles(any(), any(), any(), any());
  }

  @Test
  void favoritedFilter_resolvesToArticleIds() {
    when(userAuthClient.findByUsername("jake"))
        .thenReturn(Optional.of(new AuthorRef("user-1", "jake", "", "")));
    when(favoriteClient.articlesFavoritedByUser("user-1")).thenReturn(asList("a1", "a2"));
    when(articleReadService.queryArticles(eq(null), eq(null), eq(asList("a1", "a2")), any()))
        .thenReturn(singletonList("a1"));
    when(articleReadService.countArticle(eq(null), eq(null), eq(asList("a1", "a2")))).thenReturn(1);
    ArticleData article = baseArticle("a1", "user-2");
    when(articleReadService.findArticles(singletonList("a1")))
        .thenReturn(new ArrayList<>(singletonList(article)));
    when(userAuthClient.findByIds(singletonList("user-2")))
        .thenReturn(singletonList(new AuthorRef("user-2", "john", "", "")));
    when(favoriteClient.countsForArticles(singletonList("a1")))
        .thenReturn(singletonList(new ArticleFavoriteCount("a1", 1)));

    ArticleDataList list = service.findRecentArticles(null, null, "jake", new Page(0, 20), null);

    assertThat(list.getArticleDatas()).hasSize(1);
    assertThat(list.getArticleDatas().get(0).getProfileData().getUsername()).isEqualTo("john");
  }

  @Test
  void feed_usesFollowedAuthors() {
    User me = new User("me");
    when(profileClient.followedAuthors("me")).thenReturn(asList("user-1"));
    ArticleData article = baseArticle("a1", "user-1");
    when(articleReadService.findArticlesOfAuthors(eq(asList("user-1")), any()))
        .thenReturn(new ArrayList<>(singletonList(article)));
    when(articleReadService.countFeedSize(asList("user-1"))).thenReturn(1);
    when(userAuthClient.findByIds(singletonList("user-1")))
        .thenReturn(singletonList(new AuthorRef("user-1", "jake", "", "")));
    when(favoriteClient.countsForArticles(singletonList("a1")))
        .thenReturn(singletonList(new ArticleFavoriteCount("a1", 0)));
    when(favoriteClient.favoritedArticles("me", singletonList("a1"))).thenReturn(new ArrayList<>());
    when(profileClient.followingAmong("me", singletonList("user-1")))
        .thenReturn(singletonList("user-1"));

    ArticleDataList list = service.findUserFeed(me, new Page(0, 20));

    assertThat(list.getCount()).isEqualTo(1);
    assertThat(list.getArticleDatas().get(0).getProfileData().isFollowing()).isTrue();
  }

  @Test
  void feed_noFollows_returnsEmpty() {
    when(profileClient.followedAuthors("me")).thenReturn(new ArrayList<>());
    ArticleDataList list = service.findUserFeed(new User("me"), new Page(0, 20));
    assertThat(list.getCount()).isZero();
    verify(articleReadService, never()).findArticlesOfAuthors(any(), any());
  }
}
