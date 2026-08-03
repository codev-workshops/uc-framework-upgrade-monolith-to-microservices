package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.application.ProfileQueryService;
import io.spring.contracts.client.UserServiceClient;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.dto.UserSummary;
import io.spring.contracts.security.JwtVerifier;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.FollowRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProfileApi.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileApiTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private FollowRepository followRepository;
  @MockBean private UserServiceClient userServiceClient;
  @MockBean private JwtVerifier jwtVerifier;

  @BeforeEach
  public void setUp() {
    Mockito.when(userServiceClient.findByUsername("p"))
        .thenReturn(Optional.of(new UserSummary("user-p", "p", "bio", "image")));
    Mockito.when(profileQueryService.findByUsername(Mockito.eq("p"), any()))
        .thenReturn(Optional.of(new ProfileData("user-p", "p", "bio", "image", true)));
    Mockito.when(profileQueryService.findByUsername(Mockito.eq("missing"), any()))
        .thenReturn(Optional.empty());
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateAs(String userId) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
  }

  @Test
  public void should_get_profile_in_realworld_shape() throws Exception {
    mockMvc
        .perform(get("/profiles/p"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.profile.username").value("p"))
        .andExpect(jsonPath("$.profile.bio").value("bio"))
        .andExpect(jsonPath("$.profile.image").value("image"))
        .andExpect(jsonPath("$.profile.following").value(true));
  }

  @Test
  public void should_404_for_unknown_profile() throws Exception {
    mockMvc.perform(get("/profiles/missing")).andExpect(status().isNotFound());
  }

  @Test
  public void should_follow_user() throws Exception {
    authenticateAs("user-a");

    mockMvc
        .perform(post("/profiles/p/follow"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.profile.following").value(true));

    Mockito.verify(followRepository).saveRelation(new FollowRelation("user-a", "user-p"));
  }

  @Test
  public void should_unfollow_user() throws Exception {
    authenticateAs("user-a");
    FollowRelation relation = new FollowRelation("user-a", "user-p");
    Mockito.when(followRepository.findRelation("user-a", "user-p"))
        .thenReturn(Optional.of(relation));

    mockMvc.perform(delete("/profiles/p/follow")).andExpect(status().isOk());

    Mockito.verify(followRepository).removeRelation(relation);
  }

  @Test
  public void should_404_when_unfollowing_without_relation() throws Exception {
    authenticateAs("user-a");
    Mockito.when(followRepository.findRelation("user-a", "user-p")).thenReturn(Optional.empty());

    mockMvc.perform(delete("/profiles/p/follow")).andExpect(status().isNotFound());
  }
}
