package test;

import de.akquinet.acceptit.AcceptItRule;
import de.akquinet.acceptit.TestScoped;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.enterprise.inject.Produces;
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

    @Produces
    @TestScoped
    WebDriver createPhantomJs() {

        DesiredCapabilities sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", true);

        return new PhantomJSDriver(sCaps);
    }

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
