package test;

import de.akquinet.acceptit.AcceptItRule;
import de.akquinet.acceptit.TestListener;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

import javax.enterprise.inject.Produces;

import static org.fest.assertions.api.Assertions.assertThat;

public class ListenerTest {

    @Rule
    public final AcceptItRule acceptItRule = new AcceptItRule();

    boolean startingInvoked;
    boolean succededInvoked;
    boolean finishedInvoked;

    @Produces
    TestListener listener() {
        return new TestListener() {
            @Override
            public void starting(FrameworkMethod method) {
                assertThat(startingInvoked).isFalse();
                startingInvoked = true;
            }

            @Override
            public void succeeded(FrameworkMethod method) {
                assertThat(startingInvoked).isTrue();
                assertThat(succededInvoked).isFalse();
                succededInvoked = true;
            }

            @Override
            public void finished(FrameworkMethod method) {
                assertThat(startingInvoked).isTrue();
                assertThat(succededInvoked).isTrue();
                assertThat(finishedInvoked).isFalse();
                finishedInvoked = true;
            }
        };
    }

    @Test
    public void localListenerIsInvoked() {
    }
}
