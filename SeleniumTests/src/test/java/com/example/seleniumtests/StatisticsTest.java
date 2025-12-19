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

public class StatisticsTest {
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
    public void testStatistics() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement statisticsButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[4]")));
        statisticsButton.click();

        WebElement statistics = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-statistics/div/div[2]/mat-card[1]/mat-card-content/canvas")));
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-statistics/div/div[2]/mat-card[2]/mat-card-content/table")));
        Assertions.assertTrue(statistics.isDisplayed());
        Assertions.assertTrue(table.isDisplayed());

        driver.quit();
    }
}
