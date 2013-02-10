package test;

import de.akquinet.acceptit.AcceptItRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alphonse Bendt
 */
public class WebDriverTest {

    @Rule
    public final AcceptItRule acceptItRule = new AcceptItRule();

    @Inject
    WebDriver driver;

    @Inject
    GooglePage googlePage;

    @Test
    public void canDirectlyUseWebDriver() {
        driver.get("http://www.google.com");

        assertThat(driver.getTitle()).contains("Google");
    }

    @Test
    public void canUsePageObjectToSearchGoogle() {
        googlePage.get();
        googlePage.search("akquinet.de");

        assertThat(googlePage.getSearchResults()).contains("akquinet AG: akquinet AG Deutschland");
    }

    @Test
    public void canUsePageObjectWithPageAtomUsingBy() {
        googlePage.get();
        googlePage.searchUsingAtomBy("akquinet.de");

        assertThat(googlePage.getSearchResults()).contains("akquinet AG: akquinet AG Deutschland");
    }

    @Test
    public void canUsePageObjectWithPageAtomUsingWebElement() {
        googlePage.get();
        googlePage.searchUsingAtomWebElement("akquinet.de");

        assertThat(googlePage.getSearchResults()).contains("akquinet AG: akquinet AG Deutschland");
    }
}
