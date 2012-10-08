package org.ironrhino.core.redis;

import org.ironrhino.core.util.SerializationUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class ObjectRedisSerializer implements RedisSerializer<Object> {

	@Override
	public Object deserialize(byte[] objectData) throws SerializationException {
		return SerializationUtils.deserialize(objectData);
	}

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		return SerializationUtils.serialize(object);
	}

}
