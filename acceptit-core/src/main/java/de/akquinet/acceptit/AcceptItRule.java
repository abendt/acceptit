package de.akquinet.acceptit;

import org.jboss.solder.beanManager.BeanManagerUtils;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
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
            final AcceptItExtension extension = BeanManagerUtils.getContextualInstance(beanManager, AcceptItExtension.class);
            final Map testScopeStorage = new HashMap();

            extension.startTestScope(testScopeStorage);

            try {
                withScope(beanManager);
            } finally {
                extension.stopTestScope(testScopeStorage);
            }
        }

        private void withScope(BeanManager beanManager) throws Throwable {
            final CreationalContext<Object> creationalContext = BeanManagerUtils.injectNonContextualInstance(beanManager, target);

            try {
                base.evaluate();
            } finally {
                creationalContext.release();
            }
        }
    }
}
