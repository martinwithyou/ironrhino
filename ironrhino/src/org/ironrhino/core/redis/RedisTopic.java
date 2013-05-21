package org.ironrhino.core.redis;

import java.io.Serializable;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ironrhino.core.metadata.Scope;
import org.ironrhino.core.util.AppInfo;
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
		Topic topic = new ChannelTopic(getChannelName(Scope.GLOBAL) + "*");
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

	protected String getChannelName(Scope scope) {
		if (scope == null || scope == Scope.LOCAL)
			return null;
		StringBuilder sb = new StringBuilder(channelName).append("@");
		if (scope == Scope.APPLICATION)
			sb.append(AppInfo.getAppName());
		return sb.toString();
	}

	public void publish(T message, Scope scope) {
		if (scope == null || scope == Scope.LOCAL)
			subscribe(message);
		else
			redisTemplate.convertAndSend(getChannelName(scope), message);
	}

}
