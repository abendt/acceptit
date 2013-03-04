package de.akquinet.acceptit.jms;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Documented
@Qualifier
public @interface MonitoredDestination {

    @Nonbinding String name();

    @Nonbinding Class<? extends MessageConverter> converter() default NoConverter.class;

    @Nonbinding String factory() default "ConnectionFactory";
}
