package de.akquinet.acceptit.jms;

import javax.annotation.Nullable;
import javax.jms.Message;

public class NoConverter implements MessageConverter<Message> {
    @Override
    public Message apply(@Nullable Message message) {
        return message;
    }
}
