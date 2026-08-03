package io.spring.selenium.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.ProfilePage;
import io.spring.selenium.utils.ApiClient;
import org.testng.annotations.Test;

/** Follow / unfollow an author — profile-service behind the gateway. */
public class FollowFlowTest extends BaseTest {

  @Test(groups = {"e2e"})
  public void followThenUnfollowAnAuthor() {
    createTest(
        "followThenUnfollowAnAuthor",
        "Following flips the profile button and the follow graph in profile-service");

    ApiClient api = new ApiClient(apiUrl());
    String username = "e2efollow" + System.currentTimeMillis();
    String email = username + "@example.com";
    api.register(username, email, "password123");
    String token = api.login(email, "password123");

    new LoginPage(driver, baseUrl()).open().login(email, "password123", username);

    ProfilePage profile = new ProfilePage(driver, baseUrl()).open("janedoe");
    assertFalse(profile.isFollowing(), "a fresh user does not follow janedoe yet");
    assertFalse(api.isFollowing("janedoe", token), "profile-service agrees: not following");

    profile.toggleFollow();
    assertTrue(profile.isFollowing(), "button should read Unfollow after following");
    assertTrue(api.isFollowing("janedoe", token), "profile-service records the follow");
    attachScreenshot("followed");

    profile.toggleFollow();
    assertFalse(profile.isFollowing(), "button should read Follow again after unfollowing");
    assertFalse(api.isFollowing("janedoe", token), "profile-service records the unfollow");
    attachScreenshot("unfollowed");
  }
}
