package de.akquinet.acceptit.webdriver;

import de.akquinet.acceptit.TestScoped;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

/**
 * @author Alphonse Bendt
 */
public class WebDriverProvider {

//    @Produces
    @TestScoped
    public WebDriver createFirefox() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("intl.accept_languages", "de");

        final WebDriver driver = new FirefoxDriver(profile);

        return driver;
    }


    @Produces
    @TestScoped
    public WebDriver createPhantomJs() {
        DesiredCapabilities sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", true);

        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/usr/local/bin/phantomjs");

        return new PhantomJSDriver(sCaps);
    }

    @TestScoped
    public void disposeWebDriver(@Disposes WebDriver driver) {
        driver.quit();
    }
}
