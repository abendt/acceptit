package de.akquinet.acceptit;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alphonse Bendt
 */
public class AcceptItRule implements MethodRule {

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new TestExecutionWithWeldInjection(target, base);
    }

    private static class TestExecutionWithWeldInjection extends Statement {

        private final Object target;
        private final Statement base;

        public TestExecutionWithWeldInjection(Object target, Statement base) {
            this.target = target;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            final Weld weld = new Weld();
            final WeldContainer container = weld.initialize();

            try {
                withWeld(container);
            } finally {
                weld.shutdown();
            }
        }

        private void withWeld(WeldContainer container) throws Throwable {
            final BeanManager beanManager = container.getBeanManager();
            final AcceptItExtension extension = getInstanceByType(beanManager, AcceptItExtension.class);
            final Map testScopeStorage = new HashMap();

            extension.startTestScope(testScopeStorage);

            try {
                withScope(beanManager);
            } finally {
                extension.stopTestScope(testScopeStorage);
            }
        }

        private void withScope(BeanManager beanManager) throws Throwable {
            final Class testClazz = target.getClass();
            final AnnotatedType type = beanManager.createAnnotatedType(testClazz);
            final InjectionTarget it = beanManager.createInjectionTarget(type);
            final CreationalContext<Object> creationalContext = beanManager.createCreationalContext(null);

            try {
                it.inject(target, creationalContext);

                base.evaluate();
            } finally {
                creationalContext.release();
            }
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
    }
}
