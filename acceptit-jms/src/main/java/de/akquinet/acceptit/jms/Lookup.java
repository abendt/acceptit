package de.akquinet.acceptit.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

public interface Lookup {
    Destination lookupDestination(String name);
    ConnectionFactory lookupConnectionFactory(String name);
}
