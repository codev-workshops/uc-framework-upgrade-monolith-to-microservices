package io.spring.selenium.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Thin REST client for the gateway (:8080). Used by the E2E tests for fixture setup (creating
 * users/articles) and for asserting that what the UI shows matches what the composed backend
 * actually stores.
 */
public class ApiClient {

  private final String apiUrl;

  public ApiClient(String apiUrl) {
    this.apiUrl = apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
  }

  public String register(String username, String email, String password) {
    String body =
        String.format(
            "{\"user\":{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}}",
            username, email, password);
    return extract(send("POST", "/users", body, null), "token");
  }

  public String login(String email, String password) {
    String body =
        String.format("{\"user\":{\"email\":\"%s\",\"password\":\"%s\"}}", email, password);
    return extract(send("POST", "/users/login", body, null), "token");
  }

  public String createArticle(
      String token, String title, String description, String body, String... tags) {
    StringBuilder tagList = new StringBuilder();
    for (int i = 0; i < tags.length; i++) {
      tagList.append(i == 0 ? "" : ",").append('"').append(tags[i]).append('"');
    }
    String payload =
        String.format(
            "{\"article\":{\"title\":\"%s\",\"description\":\"%s\",\"body\":\"%s\","
                + "\"tagList\":[%s]}}",
            title, description, body, tagList);
    return extract(send("POST", "/articles", payload, token), "slug");
  }

  public String getArticle(String slug) {
    return send("GET", "/articles/" + slug, null, null);
  }

  public int favoritesCount(String slug) {
    return Integer.parseInt(extractNumber(getArticle(slug), "favoritesCount"));
  }

  public String getProfile(String username, String token) {
    return send("GET", "/profiles/" + username, null, token);
  }

  public boolean isFollowing(String username, String token) {
    return getProfile(username, token).contains("\"following\":true");
  }

  public String getComments(String slug) {
    return send("GET", "/articles/" + slug + "/comments", null, null);
  }

  public String getTags() {
    return send("GET", "/tags", null, null);
  }

  public String send(String method, String path, String body, String token) {
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) new URL(apiUrl + path).openConnection();
      conn.setRequestMethod(method);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      if (token != null) {
        conn.setRequestProperty("Authorization", "Token " + token);
      }
      if (body != null) {
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
          os.write(body.getBytes(StandardCharsets.UTF_8));
        }
      }
      int status = conn.getResponseCode();
      InputStream stream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
      String response = stream == null ? "" : read(stream);
      if (status >= 400) {
        throw new IllegalStateException(
            method + " " + path + " failed with HTTP " + status + ": " + response);
      }
      return response;
    } catch (IOException e) {
      throw new IllegalStateException(method + " " + path + " failed: " + e.getMessage(), e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private static String read(InputStream stream) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n"));
    }
  }

  private static String extract(String json, String field) {
    String marker = "\"" + field + "\":\"";
    int start = json.indexOf(marker);
    if (start < 0) {
      throw new IllegalStateException("field " + field + " not found in: " + json);
    }
    start += marker.length();
    return json.substring(start, json.indexOf('"', start));
  }

  private static String extractNumber(String json, String field) {
    String marker = "\"" + field + "\":";
    int start = json.indexOf(marker);
    if (start < 0) {
      throw new IllegalStateException("field " + field + " not found in: " + json);
    }
    start += marker.length();
    int end = start;
    while (end < json.length()
        && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
      end++;
    }
    return json.substring(start, end);
  }
}
