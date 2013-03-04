package de.akquinet.acceptit.jms;

import com.google.common.base.Function;

import javax.jms.Message;

public interface MessageConverter<T> extends Function<Message, T> {
}
