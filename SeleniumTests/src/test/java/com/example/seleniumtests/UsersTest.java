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

public class UsersTest {
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
    public void testTableOfUsersDisplayed() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement usersButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[2]")));
        usersButton.click();

        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-user-list/div/div[3]/table")));
        Assertions.assertTrue(table.isDisplayed());

        driver.quit();
    }

    @Test
    public void testDetailsOfUserDisplayed() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement usersButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[2]")));
        usersButton.click();

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-user-list/div/div[3]/table/tbody/tr[2]/td[2]")));

        WebElement detailsButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-user-list/div/div[3]/table/tbody/tr[2]/td[6]/button[1]")));
        detailsButton.click();

        WebElement informationsTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-user-details-dialog/div/mat-dialog-content/div/mat-tab-group/div/mat-tab-body[1]/div")));

        WebElement budgetButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-user-details-dialog/div/mat-dialog-content/div/mat-tab-group/mat-tab-header/div[2]/div/div/div[2]")));
        budgetButton.click();
        WebElement budgetTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-user-details-dialog/div/mat-dialog-content/div/mat-tab-group/div/mat-tab-body[2]/div")));

        WebElement depensesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-user-details-dialog/div/mat-dialog-content/div/mat-tab-group/mat-tab-header/div[2]/div/div/div[3]")));
        depensesButton.click();
        WebElement depensesTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-user-details-dialog/div/mat-dialog-content/div/mat-tab-group/div/mat-tab-body[3]/div")));

        WebElement progressionButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-user-details-dialog/div/mat-dialog-content/div/mat-tab-group/mat-tab-header/div[2]/div/div/div[4]")));
        progressionButton.click();
        WebElement progressionTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-user-details-dialog/div/mat-dialog-content/div/mat-tab-group/div/mat-tab-body[4]/div")));

        Assertions.assertTrue(informationsTab.isDisplayed());
        Assertions.assertTrue(budgetTab.isDisplayed());
        Assertions.assertTrue(depensesTab.isDisplayed());
        Assertions.assertTrue(progressionTab.isDisplayed());

        driver.quit();
    }

    @Test
    public void testActiveInactiveUser() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        By usersButtonLocator = By.xpath(
                "/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[2]"
        );

        By statusSpanLocator = By.xpath(
                "/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-user-list/div/div[3]/table/tbody/tr[2]/td[5]/mat-chip/span[2]/span/span"
        );

        By actionButtonLocator = By.xpath(
                "/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-user-list/div/div[3]/table/tbody/tr[2]/td[6]/button[2]"
        );

        By confirmButtonLocator = By.xpath(
                "/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-confirm-dialog/div/div[3]/button[2]"
        );

        wait.until(ExpectedConditions.visibilityOfElementLocated(usersButtonLocator)).click();

        String initialStatus = wait
                .until(ExpectedConditions.visibilityOfElementLocated(statusSpanLocator))
                .getText();

        wait.until(ExpectedConditions.elementToBeClickable(actionButtonLocator)).click();
        wait.until(ExpectedConditions.elementToBeClickable(confirmButtonLocator)).click();

        wait.until(d ->
                !d.findElement(statusSpanLocator).getText().equals(initialStatus)
        );

        String newStatus = driver.findElement(statusSpanLocator).getText();

        if (initialStatus.equals("Inactif")) {
            Assertions.assertEquals("Actif", newStatus);
        } else if (initialStatus.equals("Actif")) {
            Assertions.assertEquals("Inactif", newStatus);
        }

        driver.quit();
    }

}
