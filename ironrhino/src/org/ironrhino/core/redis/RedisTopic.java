package org.ironrhino.core.redis;

import java.io.Serializable;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ironrhino.core.util.ReflectionUtils;
import org.springframework.data.keyvalue.redis.connection.Message;
import org.springframework.data.keyvalue.redis.connection.MessageListener;
import org.springframework.data.keyvalue.redis.core.RedisTemplate;
import org.springframework.data.keyvalue.redis.listener.ChannelTopic;
import org.springframework.data.keyvalue.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.keyvalue.redis.listener.Topic;

public abstract class RedisTopic<T extends Serializable> implements
		MessageListener, org.ironrhino.core.message.Topic<T> {

	protected String channel;

	@Inject
	private RedisMessageListenerContainer messageListenerContainer;

	@Inject
	private RedisTemplate redisTemplate;

	public RedisTopic() {
		Class clazz = ReflectionUtils.getGenericClass(getClass());
		channel = clazz.getName();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		Topic topic = new ChannelTopic(channel);
		messageListenerContainer.addMessageListener(this,
				Collections.singleton(topic));
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		subscribe((T) redisTemplate.getValueSerializer().deserialize(
				message.getBody()));
	}

	public void publish(T message) {
		redisTemplate.convertAndSend(channel, message);
	}

}
