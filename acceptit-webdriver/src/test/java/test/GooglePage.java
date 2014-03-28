package test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import de.akquinet.acceptit.webdriver.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alphonse Bendt
 */
@PageObject
public class GooglePage extends LoadableComponent<GooglePage> {

    @Inject
    private WebDriver driver;

    @FindBy(name = "q")
    private WebElement searchField;

    @FindBy(css = ".r a")
    private List<WebElement> searchResults;

    @Inject
    @FindBy(id = "gbfwa")
    private SearchFieldUsingNestedBy searchFieldUsingNestedBy;

    @Inject
    @FindBy(name = "q")
    private SearchFieldUsingBy searchFieldUsingBy;

    @Inject
    @FindBy(name = "q")
    private SearchFieldUsingWebElement searchFieldUsingWebElement;

    @Inject
    @FindBy(id = "gbfwa")
    private SearchFieldUsingNestedWebElement searchFieldUsingNestedWebElement;

    @PostConstruct
    void validateInjection() {
        assertThat(driver).isNotNull();
        assertThat(searchField).isNotNull();
    }

    @Override
    protected void load() {
        driver.get("http://www.google.com");

        new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return input.findElement(By.name("q")).isDisplayed();
            }
        });
    }

    @Override
    protected void isLoaded() throws Error {
        try {
            assertThat(driver.findElement(By.name("q")).isDisplayed()).isTrue();
        } catch (NoSuchElementException e) {
            throw new AssertionError();
        }
    }

    public void searchUsingAtomBy(String query) {
        searchFieldUsingBy.search(query);

        waitUntilSearchResultIsDisplayed();
    }

    public void searchUsingAtomWebElement(String query) {
        searchFieldUsingWebElement.search(query);

        waitUntilSearchResultIsDisplayed();
    }

    public int countInputsUsingNestedWebElement() {
        return searchFieldUsingNestedWebElement.getNumberOfInputs();
    }

    public int countInputsUsingNestedBy() {
        return searchFieldUsingNestedBy.getNumberOfInputs();
    }

    private void waitUntilSearchResultIsDisplayed() {
        new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return !searchResults.isEmpty();
            }
        });
    }

    public void search(String query) {
        searchField.sendKeys(query);
        searchField.submit();

        waitUntilSearchResultIsDisplayed();
    }

    public List<String> getSearchResults() {
        Function<WebElement, String> transformToText = new Function<WebElement, String>() {
            @Override
            public String apply(WebElement from) {
                return from.getText();
            }
        };

        return Lists.transform(
                searchResults,
                transformToText
        );
    }
}
