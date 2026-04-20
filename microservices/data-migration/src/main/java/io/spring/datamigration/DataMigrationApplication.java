package io.spring.datamigration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataMigrationApplication {

  public static void main(String[] args) {
    String sourcePath = "../dev.db";
    String targetDir = "./data/";

    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("--source=")) {
        sourcePath = args[i].substring("--source=".length());
      } else if (args[i].startsWith("--target-dir=")) {
        targetDir = args[i].substring("--target-dir=".length());
      }
    }

    if (!targetDir.endsWith("/")) {
      targetDir += "/";
    }

    new File(targetDir).mkdirs();

    System.out.println("Source: " + sourcePath);
    System.out.println("Target directory: " + targetDir);

    try {
      String sourceUrl = "jdbc:sqlite:" + sourcePath;

      migrateUserData(sourceUrl, targetDir + "user.db");
      migrateArticleData(sourceUrl, targetDir + "article.db");
      migrateCommentData(sourceUrl, targetDir + "comment.db");

      System.out.println("Migration completed successfully!");
    } catch (Exception e) {
      System.err.println("Migration failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void migrateUserData(String sourceUrl, String targetPath) throws Exception {
    System.out.println("Migrating user data...");
    String targetUrl = "jdbc:sqlite:" + targetPath;

    try (Connection target = DriverManager.getConnection(targetUrl)) {
      Statement stmt = target.createStatement();
      stmt.execute("DROP TABLE IF EXISTS follows");
      stmt.execute("DROP TABLE IF EXISTS users");
      stmt.execute(
          "CREATE TABLE users ("
              + "id VARCHAR(255) PRIMARY KEY, "
              + "username VARCHAR(255) UNIQUE, "
              + "password VARCHAR(255), "
              + "email VARCHAR(255) UNIQUE, "
              + "bio TEXT, "
              + "image VARCHAR(511))");
      stmt.execute(
          "CREATE TABLE follows ("
              + "user_id VARCHAR(255) NOT NULL, "
              + "follow_id VARCHAR(255) NOT NULL)");

      try (Connection source = DriverManager.getConnection(sourceUrl)) {
        copyTable(
            source,
            target,
            "SELECT id, username, password, email, bio, image FROM users",
            "INSERT INTO users (id, username, password, email, bio, image) VALUES (?, ?, ?, ?, ?, ?)",
            6);
        copyTable(
            source,
            target,
            "SELECT user_id, follow_id FROM follows",
            "INSERT INTO follows (user_id, follow_id) VALUES (?, ?)",
            2);
      }
    }
    System.out.println("User data migration complete.");
  }

  private static void migrateArticleData(String sourceUrl, String targetPath) throws Exception {
    System.out.println("Migrating article data...");
    String targetUrl = "jdbc:sqlite:" + targetPath;

    try (Connection target = DriverManager.getConnection(targetUrl)) {
      Statement stmt = target.createStatement();
      stmt.execute("DROP TABLE IF EXISTS article_tags");
      stmt.execute("DROP TABLE IF EXISTS article_favorites");
      stmt.execute("DROP TABLE IF EXISTS tags");
      stmt.execute("DROP TABLE IF EXISTS articles");
      stmt.execute(
          "CREATE TABLE articles ("
              + "id VARCHAR(255) PRIMARY KEY, "
              + "user_id VARCHAR(255), "
              + "slug VARCHAR(255) UNIQUE, "
              + "title VARCHAR(255), "
              + "description TEXT, "
              + "body TEXT, "
              + "created_at TIMESTAMP NOT NULL, "
              + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
      stmt.execute(
          "CREATE TABLE article_favorites ("
              + "article_id VARCHAR(255) NOT NULL, "
              + "user_id VARCHAR(255) NOT NULL, "
              + "PRIMARY KEY(article_id, user_id))");
      stmt.execute(
          "CREATE TABLE tags (" + "id VARCHAR(255) PRIMARY KEY, " + "name VARCHAR(255) NOT NULL)");
      stmt.execute(
          "CREATE TABLE article_tags ("
              + "article_id VARCHAR(255) NOT NULL, "
              + "tag_id VARCHAR(255) NOT NULL)");

      try (Connection source = DriverManager.getConnection(sourceUrl)) {
        copyTable(
            source,
            target,
            "SELECT id, user_id, slug, title, description, body, created_at, updated_at FROM articles",
            "INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            8);
        copyTable(
            source,
            target,
            "SELECT article_id, user_id FROM article_favorites",
            "INSERT INTO article_favorites (article_id, user_id) VALUES (?, ?)",
            2);
        copyTable(
            source,
            target,
            "SELECT id, name FROM tags",
            "INSERT INTO tags (id, name) VALUES (?, ?)",
            2);
        copyTable(
            source,
            target,
            "SELECT article_id, tag_id FROM article_tags",
            "INSERT INTO article_tags (article_id, tag_id) VALUES (?, ?)",
            2);
      }
    }
    System.out.println("Article data migration complete.");
  }

  private static void migrateCommentData(String sourceUrl, String targetPath) throws Exception {
    System.out.println("Migrating comment data...");
    String targetUrl = "jdbc:sqlite:" + targetPath;

    try (Connection target = DriverManager.getConnection(targetUrl)) {
      Statement stmt = target.createStatement();
      stmt.execute("DROP TABLE IF EXISTS comments");
      stmt.execute(
          "CREATE TABLE comments ("
              + "id VARCHAR(255) PRIMARY KEY, "
              + "body TEXT, "
              + "article_id VARCHAR(255), "
              + "user_id VARCHAR(255), "
              + "created_at TIMESTAMP NOT NULL, "
              + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");

      try (Connection source = DriverManager.getConnection(sourceUrl)) {
        copyTable(
            source,
            target,
            "SELECT id, body, article_id, user_id, created_at, updated_at FROM comments",
            "INSERT INTO comments (id, body, article_id, user_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
            6);
      }
    }
    System.out.println("Comment data migration complete.");
  }

  private static void copyTable(
      Connection source, Connection target, String selectSql, String insertSql, int columnCount)
      throws Exception {
    int count = 0;
    try (Statement selectStmt = source.createStatement();
        ResultSet rs = selectStmt.executeQuery(selectSql);
        PreparedStatement insertStmt = target.prepareStatement(insertSql)) {
      target.setAutoCommit(false);
      while (rs.next()) {
        for (int i = 1; i <= columnCount; i++) {
          insertStmt.setObject(i, rs.getObject(i));
        }
        insertStmt.executeUpdate();
        count++;
      }
      target.commit();
      target.setAutoCommit(true);
    }
    System.out.println("  Copied " + count + " rows");
  }
}
