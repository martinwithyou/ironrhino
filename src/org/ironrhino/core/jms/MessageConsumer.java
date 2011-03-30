package org.ironrhino.core.jms;

public interface MessageConsumer {

	public void consume(Object object);

	public boolean supports(Class clazz);

}
