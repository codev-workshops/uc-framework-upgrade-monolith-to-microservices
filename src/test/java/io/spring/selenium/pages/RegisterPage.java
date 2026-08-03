package io.spring.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Sign-up page (/user/register). */
public class RegisterPage extends BasePage {

  private static final By USERNAME = By.cssSelector("input[placeholder='Username']");
  private static final By EMAIL = By.cssSelector("input[placeholder='Email']");
  private static final By PASSWORD = By.cssSelector("input[placeholder='Password']");
  private static final By SUBMIT = By.cssSelector("button[type='submit']");

  private final String baseUrl;

  public RegisterPage(WebDriver driver, String baseUrl) {
    super(driver);
    this.baseUrl = baseUrl;
  }

  public RegisterPage open() {
    driver.get(baseUrl + "/user/register");
    wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
    return this;
  }

  /** Fill in the form, submit, and wait until the navbar reflects the logged-in user. */
  public HomePage register(String username, String email, String password) {
    driver.findElement(USERNAME).sendKeys(username);
    driver.findElement(EMAIL).sendKeys(email);
    driver.findElement(PASSWORD).sendKeys(password);
    driver.findElement(SUBMIT).click();
    waitUntilLoggedInAs(username);
    HomePage home = new HomePage(driver, baseUrl);
    home.waitForArticleList();
    return home;
  }
}
