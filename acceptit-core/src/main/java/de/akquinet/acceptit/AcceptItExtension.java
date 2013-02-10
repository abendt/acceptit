package de.akquinet.acceptit;

import org.jboss.weld.context.bound.BoundRequestContextImpl;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author Alphonse Bendt
 */
public class AcceptItExtension implements Extension {

    private BoundRequestContextImpl testExecutionContext = new BoundRequestContextImpl() {
        @Override
        public Class<? extends Annotation> getScope() {
            return TestScoped.class;
        }
    };

    private void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        bbd.addScope(TestScoped.class, true, false);
    }

    private void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        abd.addContext(testExecutionContext);
    }

    void startTestScope(Map store) {
        testExecutionContext.associate(store);
        testExecutionContext.activate();
    }

    void stopTestScope(Map store) {
        testExecutionContext.invalidate();
        testExecutionContext.deactivate();
        testExecutionContext.dissociate(store);
    }
}
