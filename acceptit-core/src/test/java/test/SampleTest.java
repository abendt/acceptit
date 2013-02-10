package test;

import de.akquinet.acceptit.AcceptItRule;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alphonse Bendt
 */
public class SampleTest {

    @Rule
    public final AcceptItRule acceptItRule = new AcceptItRule();

    @Inject
    TestBean testBean1;

    @Test
    public void canInjectSimpleBean() {
        assertThat(testBean1).isNotNull();
    }

    @Inject
    TestBean testBean2;

    @Test
    public void simpleBeanIsInjectedMultipleTimes() {
        assertThat(testBean1).isNotSameAs(testBean2);
    }

    @Inject
    TestScopedTestBean scoped1;

    @Inject
    TestScopedTestBean scoped2;

    @Test
    public void scopedBeanIsInjectedOnce() {
        assertThat(scoped1).isSameAs(scoped2);
    }

}
