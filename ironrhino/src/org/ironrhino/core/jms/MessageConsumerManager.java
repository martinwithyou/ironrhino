package org.ironrhino.core.jms;

import java.util.Collections;
import java.util.List;

public class MessageConsumerManager {

	private List<MessageConsumer> consumers = Collections.EMPTY_LIST;

	public void consume(Object object) {
		if (consumers.size() == 0)
			return;
		for (MessageConsumer mc : consumers) {
			if (mc.supports(object.getClass()))
				mc.consume(object);
		}
	}

	public void setConsumers(List<MessageConsumer> consumers) {
		this.consumers = consumers;
	}

}
