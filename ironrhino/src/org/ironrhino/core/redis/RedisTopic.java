package org.ironrhino.core.redis;

import java.io.Serializable;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ironrhino.core.util.ReflectionUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.SerializationException;

@SuppressWarnings("rawtypes")
public abstract class RedisTopic<T extends Serializable> implements
		org.ironrhino.core.message.Topic<T> {

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
		Class<?> clazz = ReflectionUtils.getGenericClass(getClass());
		channelName = clazz.getName();
	}

	@PostConstruct
	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() {
		Topic topic = new ChannelTopic(channelName);
		messageListenerContainer.addMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message, byte[] pattern) {
				try {
					subscribe((T) redisTemplate.getValueSerializer()
							.deserialize(message.getBody()));
				} catch (SerializationException e) {
					// message from other app
					if (!(e.getCause() instanceof ClassNotFoundException))
						throw e;
				}
			}
		}, Collections.singleton(topic));
	}

	public void publish(T message) {
		redisTemplate.convertAndSend(channelName, message);
	}

}
