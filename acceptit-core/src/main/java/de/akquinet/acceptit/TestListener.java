package de.akquinet.acceptit;

import org.jboss.solder.core.Veto;
import org.junit.runners.model.FrameworkMethod;

@Veto
public class TestListener {

    /**
     * Invoked when a test method succeeds
     */
    public void succeeded(FrameworkMethod method) {
    }

    /**
     * Invoked when a test method fails
     */
    public void failed(Throwable e, FrameworkMethod method) {
    }

    /**
     * Invoked when a test method is about to start
     */
    public void starting(FrameworkMethod method) {
    }

    /**
     * Invoked when a test method finishes (whether passing or failing)
     */
    public void finished(FrameworkMethod method) {
    }
}
