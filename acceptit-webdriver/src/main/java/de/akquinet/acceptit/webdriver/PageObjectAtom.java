package de.akquinet.acceptit.webdriver;

import javax.enterprise.context.Dependent;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Alphonse Bendt
 */
@Target( { TYPE})
@Retention(RUNTIME)
@Documented
@Dependent
public @interface PageObjectAtom {
}
