package org.ironrhino.core.jms;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

public class SerializableObjectMessageConverter implements MessageConverter {

	public Object fromMessage(Message message) throws JMSException,
			MessageConversionException {
		if (message instanceof ObjectMessage)
			return ((ObjectMessage) message).getObject();
		return null;
	}

	public Message toMessage(Object object, Session session)
			throws JMSException, MessageConversionException {
		ObjectMessage msg = session.createObjectMessage();
		msg.setObject((Serializable) object);
		return msg;
	}

}
