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

	protected String channelName;

	@Inject
	private RedisMessageListenerContainer messageListenerContainer;

	@Inject
	private RedisTemplate redisTemplate;

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public RedisTopic() {
		Class clazz = ReflectionUtils.getGenericClass(getClass());
		channelName = clazz.getName();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		Topic topic = new ChannelTopic(channelName);
		messageListenerContainer.addMessageListener(this,
				Collections.singleton(topic));
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		subscribe((T) redisTemplate.getValueSerializer().deserialize(
				message.getBody()));
	}

	public void publish(T message) {
		redisTemplate.convertAndSend(channelName, message);
	}

}
