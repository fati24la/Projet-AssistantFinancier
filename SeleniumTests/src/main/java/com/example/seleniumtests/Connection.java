package com.example.seleniumtests;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class Connection {

    private static final String URL = "http://localhost:4200/";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private static WebDriver createDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        return new ChromeDriver(options);
    }

    private static void login(WebDriver driver, String u, String p) {
        driver.get(URL);
        driver.findElement(By.id("mat-input-0")).sendKeys(u);
        driver.findElement(By.id("mat-input-1")).sendKeys(p);
        driver.findElement(By.xpath("//form/button")).click();
    }

    public static String testConnexionSuccess(String u, String p) {
        WebDriver driver = createDriver();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        try {
            login(driver, u, p);
            return wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.xpath("//app-dashboard//h1")))
                    .getText();
        } finally {
            driver.quit();
        }
    }

    public static String testConnectionWrongPassword(String u, String p) {
        WebDriver driver = createDriver();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);

        try {
            login(driver, u, p);
            return wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.xpath("html/body/app-root/app-login/div/mat-card/mat-card-content/form/div/div")))
                    .getText();
        } finally {
            driver.quit();
        }
    }

    public static boolean testConnectionEmptyFields() {
        WebDriver driver = createDriver();
        try {
            driver.get(URL);
            return driver.findElement(By.xpath("//form/button")).isEnabled();
        } finally {
            driver.quit();
        }
    }
}
