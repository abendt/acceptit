package de.akquinet.acceptit.webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.Annotations;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Alphonse Bendt
 */
public class WebDriverExtension implements Extension {

    private final ThreadLocal<Stack<InjectionPoint>> currentInjectionPointForPageComponent = new ThreadLocal<Stack<InjectionPoint>>() {
        @Override
        protected Stack<InjectionPoint> initialValue() {
            return new Stack();
        }
    };

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, final BeanManager bm) {
        enableByLocatorInjection(abd, bm);
    }

    private void enableByLocatorInjection(AfterBeanDiscovery abd, BeanManager bm) {
        abd.addBean(createByLocatorBean(bm));
    }

    private Bean<By> createByLocatorBean(BeanManager bm) {
        final AnnotatedType<By> at = bm.createAnnotatedType(By.class);
        final InjectionTarget<By> it = bm.createInjectionTarget(at);

        return new Bean<By>() {
            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(By.class);
                types.add(Object.class);
                return types;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add(new AnnotationLiteral<Default>() {});
                qualifiers.add(new AnnotationLiteral<Any>() {});
                return qualifiers;
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            @Override
            public String getName() {
                return "locatorParam";
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public Class<?> getBeanClass() {
                return By.class;
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public boolean isNullable() {
                return false;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            @Override
            public By create(CreationalContext<By> creationalContext) {
                InjectionPoint ip = currentInjectionPointForPageComponent.get().peek();

                if (ip == null) {
                    throw new IllegalStateException("expecting InjectionPoint for PageComponent to be on the stack!");
                }

                return parseFindByAnnotationsOfCurrentInjectionPoint(ip);
            }

            private By parseFindByAnnotationsOfCurrentInjectionPoint(InjectionPoint ip) {
                return new Annotations((Field) ip.getMember()).buildBy();
            }

            @Override
            public void destroy(By instance, CreationalContext<By> creationalContext) {
            }
        };
    }

    <X> void processInjectionTarget(@Observes ProcessInjectionTarget<X> pit, final BeanManager bm) {

        final InjectionTarget<X> it = pit.getInjectionTarget();

        AnnotatedType<X> at = pit.getAnnotatedType();

        if (at.getAnnotation(PageObject.class) != null) {
            initializePageObjectElements(pit, bm, it);
        } else if (at.getAnnotation(PageComponent.class) != null) {
            trackPageComponentInjectionPoint(pit, bm, it);
        }
    }

    private <X> void trackPageComponentInjectionPoint(ProcessInjectionTarget<X> pit, final BeanManager bm, final InjectionTarget<X> it) {
        InjectionTarget<X> wrapped = new DelegatingInjectionTarget<X>(it) {

            @Override
            public X produce(CreationalContext<X> ctx) {
                InjectionPoint ip = getInstanceByType(bm, InjectionPoint.class);

                currentInjectionPointForPageComponent.get().push(ip);

                return super.produce(ctx);
            }

            @Override
            public void postConstruct(X instance) {
                super.postConstruct(instance);

                currentInjectionPointForPageComponent.get().pop();

                WebDriver driver = getWebDriver(bm);

                PageFactory.initElements(driver, instance);
            }
        };

        pit.setInjectionTarget(wrapped);
    }

    private <X> void initializePageObjectElements(ProcessInjectionTarget<X> pit, final BeanManager bm, final InjectionTarget<X> it) {
        InjectionTarget<X> wrapped = new DelegatingInjectionTarget<X>(it) {
            @Override
            public void postConstruct(X instance) {
                super.postConstruct(instance);

                WebDriver driver = getWebDriver(bm);

                PageFactory.initElements(driver, instance);
            }
        };

        pit.setInjectionTarget(wrapped);
    }

    private WebDriver getWebDriver(BeanManager bm) {
        return getInstanceByType(bm, WebDriver.class);
    }

    // copied from Weld.java
    private <T> T getInstanceByType(BeanManager manager, Class<T> type, Annotation... bindings) {
        final Bean<?> bean = manager.resolve(manager.getBeans(type, bindings));
        if (bean == null) {
            throw new UnsatisfiedResolutionException("Unable to resolve a bean for " + type + " with bindings " + Arrays.asList(bindings));
        }
        CreationalContext<?> cc = manager.createCreationalContext(bean);
        return type.cast(manager.getReference(bean, type, cc));
    }

    private static class DelegatingInjectionTarget<X> implements InjectionTarget<X> {

        private final InjectionTarget<X> delegate;

        public DelegatingInjectionTarget(InjectionTarget<X> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void inject(X instance, CreationalContext<X> ctx) {
            delegate.inject(instance, ctx);
        }

        @Override
        public void postConstruct(X instance) {
            delegate.postConstruct(instance);
        }

        @Override
        public void preDestroy(X instance) {
            delegate.dispose(instance);
        }

        @Override
        public X produce(CreationalContext<X> ctx) {
            return delegate.produce(ctx);
        }

        @Override
        public void dispose(X instance) {
            delegate.dispose(instance);
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return delegate.getInjectionPoints();
        }
    }
}
