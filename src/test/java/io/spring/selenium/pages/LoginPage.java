package io.spring.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Sign-in page (/user/login). */
public class LoginPage extends BasePage {

  private static final By EMAIL = By.cssSelector("input[placeholder='Email']");
  private static final By PASSWORD = By.cssSelector("input[placeholder='Password']");
  private static final By SUBMIT = By.cssSelector("button[type='submit']");

  private final String baseUrl;

  public LoginPage(WebDriver driver, String baseUrl) {
    super(driver);
    this.baseUrl = baseUrl;
  }

  public LoginPage open() {
    driver.get(baseUrl + "/user/login");
    wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL));
    return this;
  }

  public HomePage login(String email, String password, String expectedUsername) {
    driver.findElement(EMAIL).sendKeys(email);
    driver.findElement(PASSWORD).sendKeys(password);
    driver.findElement(SUBMIT).click();
    waitUntilLoggedInAs(expectedUsername);
    HomePage home = new HomePage(driver, baseUrl);
    home.waitForArticleList();
    return home;
  }
}
