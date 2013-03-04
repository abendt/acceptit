package test;

import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.junit.rules.ExternalResource;

public class DemoServer extends ExternalResource {

    static EmbeddedJMS jms;

    @Override
    protected void before() throws Throwable {
        jms = new EmbeddedJMS();
        jms.start();
    }

    @Override
    protected void after() {
        try {
            jms.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
