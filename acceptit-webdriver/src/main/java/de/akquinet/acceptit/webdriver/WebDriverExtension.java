package de.akquinet.acceptit.webdriver;

import org.jboss.solder.beanManager.BeanManagerUtils;
import org.jboss.weld.injection.ForwardingInjectionTarget;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.*;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author Alphonse Bendt
 */
class WebDriverExtension implements Extension {

    private final ThreadLocal<Stack<InjectionPoint>> currentInjectionPointForPageComponent = new ThreadLocal<Stack<InjectionPoint>>() {
        @Override
        protected Stack<InjectionPoint> initialValue() {
            return new Stack();
        }
    };

    private InjectionPoint getCurrentInjectionPoint() {
        InjectionPoint ip = currentInjectionPointForPageComponent.get().peek();

        if (ip == null) {
            throw new IllegalStateException("expecting InjectionPoint for PageComponent to be on the stack!");
        }
        return ip;
    }

    private void popCurrentInjectionPoint() {
        currentInjectionPointForPageComponent.get().pop();
    }

    private void pushCurrentInjectionPoint(BeanManager bm) {
        InjectionPoint ip = BeanManagerUtils.getContextualInstance(bm, InjectionPoint.class);

        currentInjectionPointForPageComponent.get().push(ip);
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, final BeanManager bm) {
        enableByInjection(abd, bm);
        enableWebElementInjection(abd, bm);
    }

    private void enableByInjection(AfterBeanDiscovery abd, BeanManager bm) {
        abd.addBean(createByBean(bm));
    }

    private void enableWebElementInjection(AfterBeanDiscovery abd, BeanManager bm) {
        abd.addBean(createWebElementBean(bm));
    }

    private Bean<WebElement> createWebElementBean(final BeanManager bm) {
        return new EmptyBean<WebElement>(bm, WebElement.class) {

            @Override
            public WebElement create(CreationalContext<WebElement> creationalContext) {
                InjectionPoint ip = getCurrentInjectionPoint();

                return createWebElementForCurrentInjectionPoint(ip);
            }

            private WebElement createWebElementForCurrentInjectionPoint(InjectionPoint ip) {

                ElementLocatorFactory factory = new DefaultElementLocatorFactory(getWebDriver(bm));
                FieldDecorator decorator = new DefaultFieldDecorator(factory) {
                    @Override
                    public Object decorate(ClassLoader loader, Field field) {
                        ElementLocator locator = factory.createLocator(field);
                        if (locator == null) {
                            return null;
                        }

                        return proxyForLocator(loader, locator);
                    }
                };

                return (WebElement) decorator.decorate(getClass().getClassLoader(), (Field) ip.getMember());
            }
        };
    }

    private Bean<By> createByBean(BeanManager bm) {
        return new EmptyBean<By>(bm, By.class) {
            @Override
            public By create(CreationalContext<By> creationalContext) {
                InjectionPoint ip = getCurrentInjectionPoint();

                return parseFindByAnnotationsOfCurrentInjectionPoint(ip);
            }

            private By parseFindByAnnotationsOfCurrentInjectionPoint(InjectionPoint ip) {
                return new Annotations((Field) ip.getMember()).buildBy();
            }
        };
    }

    <X> void processInjectionTarget(@Observes ProcessInjectionTarget<X> pit, final BeanManager bm) {

        final InjectionTarget<X> it = pit.getInjectionTarget();
        final AnnotatedType<X> at = pit.getAnnotatedType();

        if (at.getAnnotation(PageObject.class) != null) {
            initializePageObjectElements(pit, bm, it);
        } else if (at.getAnnotation(PageObjectAtom.class) != null) {
            trackInjectionPoint(pit, bm, it);
        }
    }

    private <X> void trackInjectionPoint(ProcessInjectionTarget<X> pit, final BeanManager bm, final InjectionTarget<X> it) {
        InjectionTarget<X> wrapped = new ForwardingInjectionTarget<X>() {

            @Override
            protected InjectionTarget<X> delegate() {
                return it;
            }

            @Override
            public X produce(CreationalContext<X> ctx) {
                pushCurrentInjectionPoint(bm);

                return super.produce(ctx);
            }

            @Override
            public void inject(X instance, CreationalContext<X> ctx) {
                super.inject(instance, ctx);

                WebDriver driver = getWebDriver(bm);

                MyPageFactory.initElements(driver, instance);
            }

            @Override
            public void postConstruct(X instance) {
                super.postConstruct(instance);

                popCurrentInjectionPoint();
            }
        };

        pit.setInjectionTarget(wrapped);
    }


    private <X> void initializePageObjectElements(ProcessInjectionTarget<X> pit, final BeanManager bm, final InjectionTarget<X> it) {
        InjectionTarget<X> wrapped = new ForwardingInjectionTarget<X>() {

            @Override
            protected InjectionTarget<X> delegate() {
                return it;
            }

            @Override
            public void inject(X instance, CreationalContext<X> ctx) {
                super.inject(instance, ctx);

                WebDriver driver = getWebDriver(bm);

                MyPageFactory.initElements(driver, instance);
            }
        };

        pit.setInjectionTarget(wrapped);
    }

    private WebDriver getWebDriver(BeanManager bm) {
        return BeanManagerUtils.getContextualInstance(bm, WebDriver.class);
    }

    private abstract class EmptyBean<X> implements Bean<X> {

        private final Class<X> clazz;
        private final InjectionTarget<By> it;

        public EmptyBean(BeanManager bm, Class<X> clazz) {
            this.clazz = clazz;
            AnnotatedType<By> at = bm.createAnnotatedType(By.class);
            this.it = bm.createInjectionTarget(at);
        }

        @Override
        public Set<Type> getTypes() {
            Set<Type> types = new HashSet<Type>();
            types.add(clazz);
            types.add(Object.class);
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> qualifiers = new HashSet<Annotation>();
            qualifiers.add(new AnnotationLiteral<Default>() {
            });
            qualifiers.add(new AnnotationLiteral<Any>() {
            });
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        @Override
        public String getName() {
            return clazz.getSimpleName();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public Class<?> getBeanClass() {
            return clazz;
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
        public void destroy(X instance, CreationalContext<X> creationalContext) {
        }
    }
}

/**
 * in contrast to the original implementation this modified version
 * will not inject into non null members.
 */
class MyPageFactory {

    static void initElements(WebDriver driver, Object page) {
        final WebDriver driverRef = driver;
        initElements(new DefaultElementLocatorFactory(driverRef), page);
    }

    static void initElements(ElementLocatorFactory factory, Object page) {
        final ElementLocatorFactory factoryRef = factory;
        initElements(new DefaultFieldDecorator(factoryRef), page);
    }

    static void initElements(FieldDecorator decorator, Object page) {
        Class<?> proxyIn = page.getClass();
        while (proxyIn != Object.class) {
            proxyFields(decorator, page, proxyIn);
            proxyIn = proxyIn.getSuperclass();
        }
    }

    private static void proxyFields(FieldDecorator decorator, Object page, Class<?> proxyIn) {
        Field[] fields = proxyIn.getDeclaredFields();
        for (Field field : fields) {
            Object value = decorator.decorate(page.getClass().getClassLoader(), field);
            if (value != null) {
                try {
                    field.setAccessible(true);
                    if (field.get(page) == null) {
                        field.set(page, value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

