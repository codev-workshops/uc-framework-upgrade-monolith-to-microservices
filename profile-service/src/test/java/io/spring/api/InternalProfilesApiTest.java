package io.spring.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.application.ProfileQueryService;
import io.spring.contracts.dto.ProfileData;
import io.spring.contracts.security.JwtVerifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = InternalProfilesApi.class)
@AutoConfigureMockMvc(addFilters = false)
public class InternalProfilesApiTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private JwtVerifier jwtVerifier;

  @Test
  public void should_expose_profile_lookup() throws Exception {
    Mockito.when(profileQueryService.findByUsername("p", "user-a"))
        .thenReturn(Optional.of(new ProfileData("user-p", "p", "bio", "image", true)));

    mockMvc
        .perform(get("/internal/profiles/p?currentUserId=user-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("p"))
        .andExpect(jsonPath("$.following").value(true));
  }

  @Test
  public void should_expose_follow_state() throws Exception {
    Mockito.when(profileQueryService.isFollowing("user-a", "user-p")).thenReturn(true);
    Mockito.when(profileQueryService.followingAuthors("user-a", List.of("user-p", "user-z")))
        .thenReturn(Set.of("user-p"));
    Mockito.when(profileQueryService.followedUsers("user-a")).thenReturn(List.of("user-p"));

    mockMvc
        .perform(get("/internal/follows/is-following?currentUserId=user-a&targetUserId=user-p"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(true));
    mockMvc
        .perform(
            get("/internal/follows/following-authors?currentUserId=user-a&authorIds=user-p,user-z"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("user-p"));
    mockMvc
        .perform(get("/internal/follows/followed-users?userId=user-a"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("user-p"));
  }
}
