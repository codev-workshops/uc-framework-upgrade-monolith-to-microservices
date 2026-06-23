package io.spring.favoriteservice.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.favoriteservice.JacksonCustomizations;
import io.spring.favoriteservice.core.ArticleFavorite;
import io.spring.favoriteservice.core.ArticleFavoriteRepository;
import io.spring.favoriteservice.security.WebSecurityConfig;
import io.spring.shared.client.ArticleServiceClient;
import io.spring.shared.dto.ArticleData;
import io.spring.shared.dto.ProfileData;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleFavoriteApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleFavoriteApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;

  @MockBean private ArticleServiceClient articleServiceClient;

  private ArticleData articleData;
  private String slug;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);

    slug = "title-abc123";
    articleData =
        new ArticleData(
            "article-id-1",
            slug,
            "title",
            "desc",
            "body",
            true,
            1,
            DateTime.now(),
            DateTime.now(),
            Arrays.asList("java"),
            new ProfileData("other-user-id", "other", "", "", false));
    when(articleServiceClient.getArticleBySlug(eq(slug))).thenReturn(Optional.of(articleData));
  }

  @Test
  public void should_favorite_an_article_success() throws Exception {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", slug)
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.id", equalTo(articleData.getId()));

    verify(articleFavoriteRepository).save(any());
  }

  @Test
  public void should_unfavorite_an_article_success() throws Exception {
    when(articleFavoriteRepository.find(eq(articleData.getId()), eq(userId)))
        .thenReturn(Optional.of(new ArticleFavorite(articleData.getId(), userId)));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/favorite", slug)
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.id", equalTo(articleData.getId()));
    verify(articleFavoriteRepository).remove(new ArticleFavorite(articleData.getId(), userId));
  }
}
