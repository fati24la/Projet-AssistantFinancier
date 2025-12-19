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

public class DashboardTest {

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
    public void testStates() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement stats = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-dashboard/div/div/div[1]")));
        Assertions.assertTrue(stats.isDisplayed());

        driver.quit();
    }

    @Test
    public void testCharts() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement lineChart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-dashboard/div/div/div[2]/mat-card[1]/mat-card-content/div/canvas")));
        WebElement pieChart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-dashboard/div/div/div[2]/mat-card[2]/mat-card-content/div/canvas")));
        WebElement barchart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-dashboard/div/div/div[2]/mat-card[3]/mat-card-content/div/canvas")));

        Assertions.assertTrue(lineChart.isDisplayed());
        Assertions.assertTrue(pieChart.isDisplayed());
        Assertions.assertTrue(barchart.isDisplayed());

        driver.quit();
    }
}
