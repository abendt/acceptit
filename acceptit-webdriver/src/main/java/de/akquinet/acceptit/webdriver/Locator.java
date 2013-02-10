package de.akquinet.acceptit.webdriver;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Alphonse Bendt
 */
@Target( { CONSTRUCTOR, FIELD})
@Retention(RUNTIME)
@Documented
public @interface Locator {
}
