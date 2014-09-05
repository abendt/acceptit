package test;

import de.akquinet.acceptit.AcceptItRule;
import de.akquinet.acceptit.TestScoped;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alphonse Bendt
 */
public class WebDriverTest {

    private boolean useFirefox = false;

    @Rule
    public final AcceptItRule acceptItRule = new AcceptItRule();

    @Inject
    WebDriver driver;

    @Inject
    GooglePage googlePage;

    @Produces
    @TestScoped
    WebDriver createWebDriver() {

        DesiredCapabilities sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", true);

        if (useFirefox) {
            return new FirefoxDriver();
        } else {
            return new PhantomJSDriver(sCaps);
        }
    }

    @TestScoped
    void closeDriver(@Disposes WebDriver driver) {
        System.out.println("CLOSE");

        driver.close();
        driver.quit();
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

        assertThat(googlePage.getSearchResults()).contains("akquinet AG Homepage");
    }

    @Test
    public void canUsePageObjectWithPageAtomUsingBy() {
        googlePage.get();
        googlePage.searchUsingAtomBy("akquinet.de");

        assertThat(googlePage.getSearchResults()).contains("akquinet AG Homepage");
    }

    @Test
    public void canUsePageObjectWithPageAtomUsingWebElement() {
        googlePage.get();
        googlePage.searchUsingAtomWebElement("akquinet.de");

        assertThat(googlePage.getSearchResults()).contains("akquinet AG Homepage");
    }

    @Test
    public void canUsePageObjectWithPageAtomUsingNestedWebElement() throws Exception {
        assumeUsingFirefox();

        googlePage.get();

        verifyExpectedPageStructure();

        assertThat(googlePage.countInputsUsingNestedWebElement()).isEqualTo(3);
    }

    /**
     * some Tests don't run using PhantomJS
     */
    private void assumeUsingFirefox() {
        Assume.assumeTrue(useFirefox);
    }

    private void verifyExpectedPageStructure() {
        // the PageObjects using the nested structure rely on the existence of this
        // HTML structure:
        assertThat(driver.findElements(By.cssSelector("#gbfwa input"))).hasSize(3);
    }

    @Test
    public void canUsePageObjectWithPageAtomUsingNestedBy() {
        assumeUsingFirefox();

        googlePage.get();

        verifyExpectedPageStructure();

        assertThat(googlePage.countInputsUsingNestedBy()).isEqualTo(3);
    }
}
