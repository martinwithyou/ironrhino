package org.ironrhino.core.jms;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConverter;

public class MessageConverterManager implements MessageConverter {

	public final static String CLASS_PROPERTY_NAME = "_class";

	private Map<Class, MessageConverter> converters = Collections.EMPTY_MAP;

	private MessageConverter defaultMessageConverter = new SerializableObjectMessageConverter();

	public void setDefaultMessageConverter(
			MessageConverter defaultMessageConverter) {
		this.defaultMessageConverter = defaultMessageConverter;
	}

	public Message toMessage(Object obj, Session session) throws JMSException {
		for (Class clazz : converters.keySet())
			if (clazz.isAssignableFrom(obj.getClass())) {
				Message m = converters.get(clazz).toMessage(obj, session);
				m.setStringProperty(CLASS_PROPERTY_NAME, obj.getClass()
						.getName());
				return m;
			}
		if (obj instanceof Serializable) {
			Message m = defaultMessageConverter.toMessage(obj, session);
			m.setStringProperty(CLASS_PROPERTY_NAME, obj.getClass().getName());
			return m;
		}
		throw new JMSException("Object:[" + obj + "] has not converter");
	}

	public Object fromMessage(Message msg) throws JMSException {
		Class cla = null;
		try {
			cla = Class.forName(msg.getStringProperty(CLASS_PROPERTY_NAME));
		} catch (ClassNotFoundException e) {
			JMSException ex = new JMSException("Message:[" + msg
					+ "] has not converter");
			ex.setLinkedException(e);
			throw ex;
		}
		for (Class clazz : converters.keySet())
			if (clazz.isAssignableFrom(cla))
				return converters.get(clazz).fromMessage(msg);
		if (Serializable.class.isAssignableFrom(cla))
			return defaultMessageConverter.fromMessage(msg);
		throw new JMSException("Message:[" + msg + "] has not converter");
	}

	public void setConverters(Map<Class, MessageConverter> converters) {
		this.converters = converters;
	}

}
