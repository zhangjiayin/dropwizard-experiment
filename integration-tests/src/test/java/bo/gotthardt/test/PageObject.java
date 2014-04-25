package bo.gotthardt.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class PageObject {
    protected static String BASE_URL = "http://localhost:8056/static/";

    protected WebDriver driver;

    protected PageObject(WebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
        load();
    }

    protected abstract void load();

    protected void waitFor(By locator, String message) {
        new WebDriverWait(driver, 5)
                .withMessage(message)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitFor(WebElement element, String message) {
        new WebDriverWait(driver, 5)
                .withMessage(message)
                .until(ExpectedConditions.visibilityOf(element));
    }

}