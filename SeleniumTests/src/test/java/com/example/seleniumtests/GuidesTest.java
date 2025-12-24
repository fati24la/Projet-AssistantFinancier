package com.example.seleniumtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GuidesTest {
    private WebDriver setup() {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return new ChromeDriver();
    }

    private void login(WebDriver driver) {
        driver.get("http://localhost:4200/");
        driver.findElement(By.id("mat-input-0")).sendKeys("admin");
        driver.findElement(By.id("mat-input-1")).sendKeys("123456");
        driver.findElement(By.xpath("//form/button")).click();
    }

    @Test
    public void testNewGuideSuccess() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement guidesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[3]")));
        guidesButton.click();

        WebElement newGuidesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-course-list/div/div[1]/button")));
        newGuidesButton.click();

        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/mat-form-field[1]/div[1]/div/div[2]/input")));
        titleInput.sendKeys("Test");

        WebElement descriptionInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/mat-form-field[2]/div[1]/div/div[2]/textarea")));
        descriptionInput.sendKeys("Test Description");

        WebElement contentInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/mat-form-field[3]/div[1]/div/div[2]/textarea")));
        contentInput.sendKeys("Test Content");

        WebElement categorySelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/div/mat-form-field[1]/div[1]/div/div[2]/mat-select")));
        categorySelect.click();
        WebElement categoryOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[4]/div/div/mat-option[1]")));
        categoryOption.click();

        WebElement difficultySelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/div/mat-form-field[2]/div[1]/div/div[2]/mat-select")));
        difficultySelect.click();
        WebElement difficultyOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[4]/div/div/mat-option[1]")));
        difficultyOption.click();

        WebElement languageSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/div/mat-form-field[3]/div[1]/div/div[2]/mat-select")));
        languageSelect.click();
        WebElement languageOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[4]/div/div/mat-option[1]")));
        languageOption.click();

        WebElement durationInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/div/mat-form-field[4]/div[1]/div/div[2]/input")));
        durationInput.sendKeys("20");

//        WebElement activeGuideCheckbox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/mat-checkbox/div/label/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-content/form/mat-checkbox/div/label")));
//        activeGuideCheckbox.click();
//        activeGuideCheckbox.click();

        WebElement createGuidesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-actions/button[2]")));
        createGuidesButton.click();

        WebElement createGuidesNotification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div/div/mat-snack-bar-container/div/div/div/div/simple-snack-bar/div[1]")));
        Assertions.assertEquals("Cours créé avec succès", createGuidesNotification.getText());

        driver.quit();
    }

    @Test
    public void testNewGuideEmptyFields() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement guidesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[3]")));
        guidesButton.click();

        WebElement newGuidesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-course-list/div/div[1]/button")));
        newGuidesButton.click();

        WebElement createGuidesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-actions/button[2]")));
        Assertions.assertFalse(createGuidesButton.isEnabled());

        driver.quit();
    }

    @Test
    public void testModifyGuideSuccess() {
        WebDriver driver = setup();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        login(driver);

        WebElement guidesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[3]")));
        guidesButton.click();

        WebElement modifyGuideButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-course-list/div/div[3]/table/tbody/tr[2]/td[7]/div/button[1]")));
        modifyGuideButton.click();

        WebElement modifyGuideButton2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div[2]/div/mat-dialog-container/div/div/app-course-form-dialog/mat-dialog-actions/button[2]")));
        modifyGuideButton2.click();

        WebElement createGuidesNotification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/div/div/mat-snack-bar-container/div/div/div/div/simple-snack-bar/div[1]")));
        Assertions.assertEquals("Cours modifié avec succès", createGuidesNotification.getText());
        driver.quit();
    }

//    @Test
//    public void testActiveInactiveGuide() {
//        WebDriver driver = setup();
//        driver.manage().window().maximize();
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//        login(driver);
//
//        By guidesButtonLocator = By.xpath(
//                "/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav/div/mat-nav-list/div/a[3]"
//        );
//
//        By statusSpanLocator = By.xpath(
//                "//*[@id=\"mat-mdc-chip-2\"]/span[2]/span/span"
//        );
//
//        By statusSpanLocatorAfterModification = By.xpath(
//                "//*[@id=\"mat-mdc-chip-23\"]/span[2]/span/span"
//        );
//
//        By actionButtonLocator = By.xpath(
//                "/html/body/app-root/app-admin-layout/mat-sidenav-container/mat-sidenav-content/main/app-course-list/div/div[3]/table/tbody/tr[2]/td[7]/div/button[3]"
//        );
//
//
//
//        wait.until(ExpectedConditions.visibilityOfElementLocated(guidesButtonLocator)).click();
//
//        String initialStatus = wait
//                .until(ExpectedConditions.visibilityOfElementLocated(statusSpanLocator))
//                .getText();
//
//        wait.until(ExpectedConditions.elementToBeClickable(actionButtonLocator)).click();
//
//        wait.until(d ->
//                !d.findElement(statusSpanLocatorAfterModification).getText().equals(initialStatus)
//        );
//
//        String newStatus = driver.findElement(statusSpanLocator).getText();
//
//        if (initialStatus.equals("Inactif")) {
//            Assertions.assertEquals("Actif", newStatus);
//        } else if (initialStatus.equals("Actif")) {
//            Assertions.assertEquals("Inactif", newStatus);
//        }
//
//        driver.quit();
//    }
}
