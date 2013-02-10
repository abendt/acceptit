package test;

import de.akquinet.acceptit.webdriver.PageObjectAtom;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * @author Alphonse Bendt
 */
@PageObjectAtom
public class SearchFieldUsingWebElement {

    @Inject
    WebElement searchField;

    public void search(String query) {
        searchField.sendKeys(query);
        searchField.submit();
    }
}
