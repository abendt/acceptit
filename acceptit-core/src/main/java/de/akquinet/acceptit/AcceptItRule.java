package de.akquinet.acceptit;

import org.jboss.solder.beanManager.BeanManagerUtils;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alphonse Bendt
 */
public class AcceptItRule implements MethodRule {

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new TestExecutionWithWeldInjection(base, method, target);
    }

    private static class TestExecutionWithWeldInjection extends Statement {

        private final Statement base;
        private final FrameworkMethod method;
        private final Object target;
        private BeanManager beanManager;

        public TestExecutionWithWeldInjection(Statement base, FrameworkMethod method, Object target) {
            this.base = base;
            this.method = method;
            this.target = target;
        }

        @Override
        public void evaluate() throws Throwable {
            final Weld weld = new Weld();
            final WeldContainer container = weld.initialize();
            beanManager = container.getBeanManager();

            try {
                withWeld();
            } finally {
                weld.shutdown();
            }
        }

        private void withWeld() throws Throwable {
            final AcceptItExtension extension = BeanManagerUtils.getContextualInstance(beanManager, AcceptItExtension.class);
            final Map testScopeStorage = new HashMap();

            extension.startTestScope(testScopeStorage);

            try {
                withScope();
            } finally {
                extension.stopTestScope(testScopeStorage);
            }
        }


        private void withScope() throws Throwable {
            final CreationalContext<Object> creationalContext = BeanManagerUtils.injectNonContextualInstance(beanManager, target);

            try {
                withContext();
            } finally {
                creationalContext.release();
            }
        }

        private void withContext() throws Throwable {
            TestListener listener = lookupListener();

            listener.starting(method);
            try {
                base.evaluate();
                listener.succeeded(method);
            } catch (AssumptionViolatedException e) {
                throw e;
            } catch (Throwable t) {
                listener.failed(t, method);
                throw t;
            } finally {
                listener.finished(method);
            }
        }

        private TestListener lookupListener() {
            TestListener listener = BeanManagerUtils.getContextualInstance(beanManager, TestListener.class);

            if (listener == null) {
                listener = new TestListener() {
                };
            }

            return listener;
        }
    }
}
