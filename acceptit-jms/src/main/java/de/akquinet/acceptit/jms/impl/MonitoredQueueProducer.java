package de.akquinet.acceptit.jms.impl;

import de.akquinet.acceptit.jms.Lookup;
import de.akquinet.acceptit.jms.MessageConverter;
import de.akquinet.acceptit.jms.MonitoredDestination;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.jms.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Dependent
public class MonitoredQueueProducer {

    @Produces
    @MonitoredDestination(name = "")
    public List createList(InjectionPoint ip, Lookup lookup, Session session) throws JMSException, IllegalAccessException, InstantiationException {
        MonitoredDestination config = ip.getAnnotated().getAnnotation(MonitoredDestination.class);
        System.out.println("QueueName: " + config.name());

        Destination destination = lookup.lookupDestination(config.name());

        MessageConsumer consumer = session.createConsumer(destination);

        final List result = Collections.synchronizedList(new ArrayList());

        final MessageConverter converter = config.converter().newInstance();

        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                System.out.println("RECEIVED");
                result.add(converter.apply(message));
            }
        });

        return result;
    }
}
