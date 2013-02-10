package de.akquinet.acceptit.webdriver;

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
        InjectionPoint ip = getInstanceByType(bm, InjectionPoint.class);

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
            public void postConstruct(X instance) {
                super.postConstruct(instance);

                popCurrentInjectionPoint();

                WebDriver driver = getWebDriver(bm);

                MyPageFactory.initElements(driver, instance);
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
            public void postConstruct(X instance) {
                super.postConstruct(instance);

                WebDriver driver = getWebDriver(bm);

                MyPageFactory.initElements(driver, instance);
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

class MyPageFactory {

    static void initElements(WebDriver driver, Object page) {
        final WebDriver driverRef = driver;
        initElements(new DefaultElementLocatorFactory(driverRef), page);
    }

    /**
     * Similar to the other "initElements" methods, but takes an {@link ElementLocatorFactory} which
     * is used for providing the mechanism for fniding elements. If the ElementLocatorFactory returns
     * null then the field won't be decorated.
     *
     * @param factory The factory to use
     * @param page    The object to decorate the fields of
     */
    static void initElements(ElementLocatorFactory factory, Object page) {
        final ElementLocatorFactory factoryRef = factory;
        initElements(new DefaultFieldDecorator(factoryRef), page);
    }

    /**
     * Similar to the other "initElements" methods, but takes an {@link FieldDecorator} which is used
     * for decorating each of the fields.
     *
     * @param decorator the decorator to use
     * @param page      The object to decorate the fields of
     */
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

