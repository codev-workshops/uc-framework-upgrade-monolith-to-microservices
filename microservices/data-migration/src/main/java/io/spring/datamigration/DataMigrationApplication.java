package io.spring.datamigration;

import java.io.File;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataMigrationApplication implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DataMigrationApplication.class);

  private static final String DEFAULT_SOURCE_DB = "dev.db";
  private static final String DEFAULT_TARGET_DIR = ".";

  public static void main(String[] args) {
    SpringApplication.run(DataMigrationApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    String sourceDbPath = args.length > 0 ? args[0] : DEFAULT_SOURCE_DB;
    String targetDir = args.length > 1 ? args[1] : DEFAULT_TARGET_DIR;

    File sourceFile = new File(sourceDbPath);
    if (!sourceFile.exists()) {
      logger.error("Source database not found: {}", sourceDbPath);
      System.exit(1);
    }

    logger.info("Starting data migration from: {}", sourceDbPath);
    logger.info("Target directory: {}", targetDir);

    String sourceUrl = "jdbc:sqlite:" + sourceDbPath;
    String userDbUrl = "jdbc:sqlite:" + targetDir + "/user.db";
    String articleDbUrl = "jdbc:sqlite:" + targetDir + "/article.db";
    String commentDbUrl = "jdbc:sqlite:" + targetDir + "/comment.db";

    try (Connection sourceConn = DriverManager.getConnection(sourceUrl)) {
      migrateUserDb(sourceConn, userDbUrl);
      migrateArticleDb(sourceConn, articleDbUrl);
      migrateCommentDb(sourceConn, commentDbUrl);
    }

    logger.info("Data migration completed successfully!");
  }

  private void migrateUserDb(Connection sourceConn, String targetUrl) throws SQLException {
    logger.info("Migrating user data to: {}", targetUrl);

    try (Connection targetConn = DriverManager.getConnection(targetUrl)) {
      Statement stmt = targetConn.createStatement();

      stmt.execute("DROP TABLE IF EXISTS follows");
      stmt.execute("DROP TABLE IF EXISTS users");

      stmt.execute(
          "CREATE TABLE users ("
              + "id VARCHAR(255) PRIMARY KEY, "
              + "username VARCHAR(255) UNIQUE, "
              + "password VARCHAR(255), "
              + "email VARCHAR(255) UNIQUE, "
              + "bio TEXT, "
              + "image VARCHAR(511)"
              + ")");

      stmt.execute(
          "CREATE TABLE follows ("
              + "user_id VARCHAR(255) NOT NULL, "
              + "follow_id VARCHAR(255) NOT NULL"
              + ")");

      copyTable(sourceConn, targetConn, "users", "INSERT INTO users VALUES (?, ?, ?, ?, ?, ?)", 6);
      copyTable(sourceConn, targetConn, "follows", "INSERT INTO follows VALUES (?, ?)", 2);

      logger.info("User data migration complete");
    }
  }

  private void migrateArticleDb(Connection sourceConn, String targetUrl) throws SQLException {
    logger.info("Migrating article data to: {}", targetUrl);

    try (Connection targetConn = DriverManager.getConnection(targetUrl)) {
      Statement stmt = targetConn.createStatement();

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
              + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
              + ")");

      stmt.execute(
          "CREATE TABLE article_favorites ("
              + "article_id VARCHAR(255) NOT NULL, "
              + "user_id VARCHAR(255) NOT NULL, "
              + "PRIMARY KEY(article_id, user_id)"
              + ")");

      stmt.execute(
          "CREATE TABLE tags ("
              + "id VARCHAR(255) PRIMARY KEY, "
              + "name VARCHAR(255) NOT NULL"
              + ")");

      stmt.execute(
          "CREATE TABLE article_tags ("
              + "article_id VARCHAR(255) NOT NULL, "
              + "tag_id VARCHAR(255) NOT NULL"
              + ")");

      copyTable(
          sourceConn,
          targetConn,
          "articles",
          "INSERT INTO articles VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
          8);
      copyTable(
          sourceConn,
          targetConn,
          "article_favorites",
          "INSERT INTO article_favorites VALUES (?, ?)",
          2);
      copyTable(sourceConn, targetConn, "tags", "INSERT INTO tags VALUES (?, ?)", 2);
      copyTable(
          sourceConn, targetConn, "article_tags", "INSERT INTO article_tags VALUES (?, ?)", 2);

      logger.info("Article data migration complete");
    }
  }

  private void migrateCommentDb(Connection sourceConn, String targetUrl) throws SQLException {
    logger.info("Migrating comment data to: {}", targetUrl);

    try (Connection targetConn = DriverManager.getConnection(targetUrl)) {
      Statement stmt = targetConn.createStatement();

      stmt.execute("DROP TABLE IF EXISTS comments");

      stmt.execute(
          "CREATE TABLE comments ("
              + "id VARCHAR(255) PRIMARY KEY, "
              + "body TEXT, "
              + "article_id VARCHAR(255), "
              + "user_id VARCHAR(255), "
              + "created_at TIMESTAMP NOT NULL, "
              + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
              + ")");

      copyTable(
          sourceConn, targetConn, "comments", "INSERT INTO comments VALUES (?, ?, ?, ?, ?, ?)", 6);

      logger.info("Comment data migration complete");
    }
  }

  private void copyTable(
      Connection sourceConn,
      Connection targetConn,
      String tableName,
      String insertSql,
      int columnCount)
      throws SQLException {
    logger.info("Copying table: {}", tableName);

    try (Statement sourceStmt = sourceConn.createStatement();
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM " + tableName);
        PreparedStatement insertStmt = targetConn.prepareStatement(insertSql)) {

      int rowCount = 0;
      while (rs.next()) {
        for (int i = 1; i <= columnCount; i++) {
          insertStmt.setObject(i, rs.getObject(i));
        }
        insertStmt.executeUpdate();
        rowCount++;
      }

      logger.info("Copied {} rows from {}", rowCount, tableName);
    }
  }
}
