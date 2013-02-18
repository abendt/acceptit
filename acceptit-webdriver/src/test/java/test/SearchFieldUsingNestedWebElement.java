package test;

import de.akquinet.acceptit.webdriver.PageObjectAtom;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Alphonse Bendt
 */
@PageObjectAtom
public class SearchFieldUsingNestedWebElement {

    @Inject
    WebElement root;

    @FindBy(tagName = "input")
    List<WebElement> inputsFound;

    public int getNumberOfInputs() {
        return inputsFound.size();
    }
}
