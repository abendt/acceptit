package de.akquinet.acceptit.webdriver;

import de.akquinet.acceptit.TestScoped;
import org.jboss.solder.bean.defaultbean.DefaultBean;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

/**
 * @author Alphonse Bendt
 */
class WebDriverProvider {

    @Produces
    @DefaultBean(FirefoxProfile.class)
    FirefoxProfile createProfile() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("intl.accept_languages", "de");

        return profile;
    }

    //    @Produces
    @TestScoped
    WebDriver createFirefox(FirefoxProfile profile) {
        return new FirefoxDriver(profile);
    }

    @Produces
    @TestScoped
    WebDriver createPhantomJs() {
        DesiredCapabilities sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", true);

        return new PhantomJSDriver(sCaps);
    }

    @TestScoped
    void disposeWebDriver(@Disposes WebDriver driver) {
        driver.quit();
    }

    @Produces
    @TestScoped
    @DefaultBean(Wait.class)
    Wait getWebDriverWait(WebDriver driver) {
        return new WebDriverWait(driver, 30).ignoring(NoSuchElementException.class, AssertionError.class).ignoring(StaleElementReferenceException.class);
    }

    @Produces
    @TestScoped
    @DefaultBean(Actions.class)
    Actions getActions(WebDriver driver) {
        return new Actions(driver);
    }
}
