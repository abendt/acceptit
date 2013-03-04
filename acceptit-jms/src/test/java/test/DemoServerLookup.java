package test;

import de.akquinet.acceptit.jms.Lookup;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

public class DemoServerLookup implements Lookup {
    @Override
    public Destination lookupDestination(String name) {
        return (Destination) DemoServer.jms.lookup(name);
    }

    @Override
    public ConnectionFactory lookupConnectionFactory(String name) {
        return (ConnectionFactory) DemoServer.jms.lookup(name);
    }
}
