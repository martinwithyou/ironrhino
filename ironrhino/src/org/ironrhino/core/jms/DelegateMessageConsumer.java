package org.ironrhino.core.jms;

import java.util.Collections;
import java.util.List;

public class DelegateMessageConsumer implements MessageConsumer {

	private List<MessageConsumer> consumers = Collections.EMPTY_LIST;

	public void setConsumers(List<MessageConsumer> consumers) {
		this.consumers = consumers;
	}

	public void consume(Object object) {
		if (consumers.size() == 0)
			return;
		for (MessageConsumer mc : consumers) {
			if (mc.supports(object.getClass()))
				mc.consume(object);
		}
	}

	public boolean supports(Class clazz) {
		return false;
	}

}
