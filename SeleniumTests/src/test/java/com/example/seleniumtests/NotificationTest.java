package com.example.seleniumtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NotificationTest {
    private WebDriver setup() {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    private void login(WebDriver driver) {
        driver.get("http://localhost:4200/");
        driver.findElement(By.id("mat-input-0")).sendKeys("admin");
        driver.findElement(By.id("mat-input-1")).sendKeys("123456");
        driver.findElement(By.xpath("//form/button")).click();
    }
    @Test
    public void testNotificationSuccess() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement notificationsButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[5]")));
        notificationsButton.click();

        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"mat-input-2\"]")));
        title.sendKeys("Test");
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"mat-input-3\"]")));
        message.sendKeys("Test");

        WebElement sendNotificationButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-notifications/div/div/mat-card[1]/mat-card-content/form/button")));
        sendNotificationButton.click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WebElement notifTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-notifications/div/div/mat-card[2]/mat-card-content/div/div/div[1]/div[1]/h4")));
        Assertions.assertEquals("Test", notifTitle.getText());
        driver.quit();
    }

    @Test
    public void testNotificationEmptyFields() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement notificationsButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[5]")));
        notificationsButton.click();


        WebElement sendNotificationButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-notifications/div/div/mat-card[1]/mat-card-content/form/button")));

        Assertions.assertFalse(sendNotificationButton.isEnabled());
        driver.quit();
    }
}
