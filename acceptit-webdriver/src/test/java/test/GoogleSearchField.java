package test;

import de.akquinet.acceptit.webdriver.PageComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * @author Alphonse Bendt
 */
@PageComponent
public class GoogleSearchField {

    @Inject
    By locator;

    @Inject
    WebDriver driver;

    public void search(String query) {
        WebElement searchField = driver.findElement(locator);

        searchField.sendKeys(query);
        searchField.submit();
    }
}
