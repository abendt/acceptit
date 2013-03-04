package test;

import de.akquinet.acceptit.AcceptItRule;
import de.akquinet.acceptit.jms.MonitoredDestination;
import de.akquinet.acceptit.jms.SendsTo;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.jms.*;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class MessagingTest {

    @Rule
    public final AcceptItRule acceptItRule = new AcceptItRule();

    @Rule
    public final DemoServer demoServer = new DemoServer();

    @Test
    public void canStartServer() {
        Destination destination = (Destination) demoServer.jms.lookup("/queue/exampleQueue");

        assertThat(destination).isNotNull();
    }

    @Inject
    @MonitoredDestination(name = "/queue/exampleQueue", factory = "ConnectionFactory")
    List<Message> exampleQueue;

    @Test
    public void canInjectQueue() {
        assertThat(exampleQueue).isNotNull();
    }

    @Inject
    Session queueSession;

    @Inject
    @SendsTo("/queue/exampleQueue")
    MessageProducer queueSender;

    @Test
    public void canSendMessageToQueue() throws JMSException, InterruptedException {
        assertThat(queueSession).isNotNull();
        assertThat(queueSender).isNotNull();

        queueSender.send(queueSession.createMessage());

        assertThatDestinationIsNotEmptyAfterSomeRetries(exampleQueue);
    }

    @Inject
    @SendsTo("/topic/exampleTopic")
    MessageProducer topicSender;

    @Inject
    @MonitoredDestination(name = "/topic/exampleTopic", factory = "ConnectionFactory")
    List<Message> exampleTopic;

    @Test
    public void canSendMessageToTopic() throws Exception {
        topicSender.send(queueSession.createMessage());

        assertThatDestinationIsNotEmptyAfterSomeRetries(exampleTopic);
    }

    void assertThatDestinationIsNotEmptyAfterSomeRetries(List list) {
        long now = System.currentTimeMillis();
        long timeout = now + 60000;

        AssertionError assertionError = null;
        while (System.currentTimeMillis() < timeout) {
            try {
                assertThat(list).isNotEmpty();

                return;
            } catch (AssertionError e) {
                assertionError = e;
            }
        }

        if (assertionError == null) {
            throw new AssertionError("should not happen");
        }

        throw assertionError;
    }
}
