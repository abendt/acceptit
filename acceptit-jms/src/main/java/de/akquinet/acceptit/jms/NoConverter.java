package de.akquinet.acceptit.jms;

import javax.jms.Message;

public class NoConverter implements MessageConverter<Message> {
    @Override
    public Message apply(Message message) {
        return message;
    }
}
