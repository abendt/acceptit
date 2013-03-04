package de.akquinet.acceptit.jms.impl;

import de.akquinet.acceptit.TestScoped;
import de.akquinet.acceptit.jms.Lookup;
import de.akquinet.acceptit.jms.SendsTo;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.jms.*;

public class MessagingProducer {

    @Produces
    @TestScoped
    ConnectionFactory getFactory(Lookup lookup) {
        System.out.println("FACTORY");
        return lookup.lookupConnectionFactory("ConnectionFactory");
    }

    @Produces
    @TestScoped
    QueueConnection openConnection(ConnectionFactory factory) throws JMSException {
        System.out.println("CONNECTION");
        Connection connection = factory.createConnection();

        connection.start();

        return (QueueConnection)connection;
    }

    @TestScoped
    void closeConnection(@Disposes Connection connection) throws JMSException {
        System.out.println("CLOSE CONNECTION");
        connection.stop();
        connection.close();
    }

    @TestScoped
    void disposeSession(@Disposes Session session) throws JMSException {
        System.out.println("CLOSE SESSION");
        session.close();
    }

    @Produces
    @TestScoped
    Session createQueueSession(Connection connection) throws JMSException {
        System.out.println("SESSION");
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Produces
    @SendsTo("")
    MessageProducer createQueueSender(InjectionPoint ip, Session session, Lookup lookup) throws JMSException {
        System.out.println("SENDER");
        SendsTo sendTo = ip.getAnnotated().getAnnotation(SendsTo.class);

        return session.createProducer(lookup.lookupDestination(sendTo.value()));
    }
}
