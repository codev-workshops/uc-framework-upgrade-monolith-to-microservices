package io.spring.favorite.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.favorite.application.data.ArticleFavoriteCount;
import io.spring.favorite.application.data.FavoriteState;
import io.spring.favorite.core.ArticleFavorite;
import io.spring.favorite.core.ArticleFavoriteRepository;
import io.spring.favorite.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FavoriteServiceTest {
  @Mock private ArticleFavoriteRepository repository;
  @Mock private ArticleFavoritesReadService readService;
  private FavoriteService favoriteService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    favoriteService = new FavoriteService(repository, readService);
  }

  @Test
  void favorite_saves_and_returns_state() {
    when(readService.isUserFavorite("u1", "a1")).thenReturn(true);
    when(readService.articleFavoriteCount("a1")).thenReturn(3);

    FavoriteState state = favoriteService.favorite("a1", "u1");

    ArgumentCaptor<ArticleFavorite> captor = ArgumentCaptor.forClass(ArticleFavorite.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getArticleId()).isEqualTo("a1");
    assertThat(captor.getValue().getUserId()).isEqualTo("u1");
    assertThat(state.getArticleId()).isEqualTo("a1");
    assertThat(state.isFavorited()).isTrue();
    assertThat(state.getFavoritesCount()).isEqualTo(3);
  }

  @Test
  void unfavorite_removes_when_present_and_returns_state() {
    ArticleFavorite existing = new ArticleFavorite("a1", "u1");
    when(repository.find("a1", "u1")).thenReturn(Optional.of(existing));
    when(readService.isUserFavorite("u1", "a1")).thenReturn(false);
    when(readService.articleFavoriteCount("a1")).thenReturn(0);

    FavoriteState state = favoriteService.unfavorite("a1", "u1");

    verify(repository).remove(existing);
    assertThat(state.isFavorited()).isFalse();
    assertThat(state.getFavoritesCount()).isEqualTo(0);
  }

  @Test
  void unfavorite_noop_when_absent() {
    when(repository.find("a1", "u1")).thenReturn(Optional.empty());
    when(readService.isUserFavorite("u1", "a1")).thenReturn(false);
    when(readService.articleFavoriteCount("a1")).thenReturn(0);

    favoriteService.unfavorite("a1", "u1");

    verify(repository, never()).remove(any());
  }

  @Test
  void counts_fills_zero_for_missing_ids() {
    when(readService.articlesFavoriteCount(any()))
        .thenReturn(Collections.singletonList(new ArticleFavoriteCount("a1", 2)));

    List<ArticleFavoriteCount> result = favoriteService.counts(Arrays.asList("a1", "a2"));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo("a1");
    assertThat(result.get(0).getCount()).isEqualTo(2);
    assertThat(result.get(1).getId()).isEqualTo("a2");
    assertThat(result.get(1).getCount()).isEqualTo(0);
  }

  @Test
  void counts_empty_input_returns_empty() {
    assertThat(favoriteService.counts(Collections.emptyList())).isEmpty();
    verify(readService, never()).articlesFavoriteCount(any());
  }

  @Test
  void status_delegates_to_read_service() {
    when(readService.userFavorites(eq(Arrays.asList("a1", "a2")), eq("u1")))
        .thenReturn(Collections.singletonList("a1"));

    List<String> result = favoriteService.status("u1", Arrays.asList("a1", "a2"));

    assertThat(result).containsExactly("a1");
  }

  @Test
  void status_empty_input_returns_empty() {
    assertThat(favoriteService.status("u1", Collections.emptyList())).isEmpty();
    verify(readService, never()).userFavorites(any(), any());
  }

  @Test
  void isFavorite_and_favoritedByUser_delegate() {
    when(readService.isUserFavorite("u1", "a1")).thenReturn(true);
    when(readService.articlesFavoritedByUser("u1")).thenReturn(Arrays.asList("a1", "a2"));

    assertThat(favoriteService.isFavorite("u1", "a1")).isTrue();
    assertThat(favoriteService.favoritedByUser("u1")).containsExactly("a1", "a2");
    verify(readService, times(1)).isUserFavorite("u1", "a1");
  }
}
