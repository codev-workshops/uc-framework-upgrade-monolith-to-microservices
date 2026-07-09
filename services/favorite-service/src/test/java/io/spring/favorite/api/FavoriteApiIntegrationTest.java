package io.spring.favorite.api;

import static io.spring.favorite.JwtTestSupport.tokenFor;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.favorite.client.UserAuthClient;
import java.io.File;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FavoriteApiIntegrationTest {

  static {
    // Start each run from a clean, freshly-migrated + seeded database.
    new File("build/favorite-test.db").delete();
  }

  @Autowired private MockMvc mvc;
  @MockBean private UserAuthClient userAuthClient;

  private static final String BEARER = "Bearer ";

  @Test
  void favorite_then_unfavorite_returns_state_only() throws Exception {
    // dedicated ids with no seed favorites, so this test is self-contained.
    mvc.perform(
            post("/articles/{id}/favorite", "article-fav-test")
                .header("Authorization", BEARER + tokenFor("user-fav-test")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.articleId", is("article-fav-test")))
        .andExpect(jsonPath("$.favorited", is(true)))
        .andExpect(jsonPath("$.favoritesCount", is(1)))
        .andExpect(jsonPath("$.author").doesNotExist());

    mvc.perform(
            delete("/articles/{id}/favorite", "article-fav-test")
                .header("Authorization", BEARER + tokenFor("user-fav-test")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.articleId", is("article-fav-test")))
        .andExpect(jsonPath("$.favorited", is(false)))
        .andExpect(jsonPath("$.favoritesCount", is(0)));
  }

  @Test
  void favorite_is_idempotent() throws Exception {
    mvc.perform(
            post("/articles/{id}/favorite", "article-idem")
                .header("Authorization", BEARER + tokenFor("user-idem")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favoritesCount", is(1)));
    mvc.perform(
            post("/articles/{id}/favorite", "article-idem")
                .header("Authorization", BEARER + tokenFor("user-idem")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favoritesCount", is(1)));
  }

  @Test
  void favorite_requires_auth() throws Exception {
    mvc.perform(post("/articles/{id}/favorite", "article-1")).andExpect(status().isUnauthorized());
  }

  @Test
  void internal_counts_fills_zero_for_unknown_ids() throws Exception {
    mvc.perform(
            post("/internal/favorites/counts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"article-1\",\"article-2\",\"article-unknown\"]"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].id", is("article-1")))
        .andExpect(jsonPath("$[0].count", is(2)))
        .andExpect(jsonPath("$[1].id", is("article-2")))
        .andExpect(jsonPath("$[1].count", is(1)))
        .andExpect(jsonPath("$[2].id", is("article-unknown")))
        .andExpect(jsonPath("$[2].count", is(0)));
  }

  @Test
  void internal_single_count() throws Exception {
    mvc.perform(get("/internal/favorites/count/{id}", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is("article-1")))
        .andExpect(jsonPath("$.count", is(2)));
  }

  @Test
  void internal_status_returns_subset_favorited_by_user() throws Exception {
    // user-2 favorited article-1 and article-3 (seed).
    mvc.perform(
            post("/internal/favorites/status")
                .param("userId", "user-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"article-1\",\"article-2\",\"article-3\"]"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", containsInAnyOrder("article-1", "article-3")));
  }

  @Test
  void internal_is_favorite() throws Exception {
    mvc.perform(
            get("/internal/favorites/is-favorite").param("userId", "user-1").param("articleId", "article-2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favorited", is(true)));
    mvc.perform(
            get("/internal/favorites/is-favorite").param("userId", "user-1").param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favorited", is(false)));
  }

  @Test
  void internal_by_user_with_user_id() throws Exception {
    // user-1 favorited article-2 and article-4 (seed).
    mvc.perform(get("/internal/favorites/by-user").param("userId", "user-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", containsInAnyOrder("article-2", "article-4")));
  }

  @Test
  void internal_by_user_resolves_username_via_userauth_client() throws Exception {
    org.mockito.Mockito.when(userAuthClient.findUserIdByUsername("jake"))
        .thenReturn(Optional.of("user-3"));
    // user-3 favorited article-1 and article-5 (seed).
    mvc.perform(get("/internal/favorites/by-user").param("username", "jake"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", containsInAnyOrder("article-1", "article-5")));
  }

  @Test
  void internal_by_user_unknown_username_is_404() throws Exception {
    org.mockito.Mockito.when(userAuthClient.findUserIdByUsername("ghost"))
        .thenReturn(Optional.empty());
    mvc.perform(get("/internal/favorites/by-user").param("username", "ghost"))
        .andExpect(status().isNotFound());
  }
}
