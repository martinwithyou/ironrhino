package org.ironrhino.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SerializationUtils {

	public static void serialize(Object obj, OutputStream outputStream) {
		if (outputStream == null) {
			throw new IllegalArgumentException(
					"The OutputStream must not be null");
		}
		Kryo kryo = new Kryo();
		Output output = new Output(outputStream);
		kryo.writeClassAndObject(output, obj);
		output.close();
	}

	public static byte[] serialize(Object obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		serialize(obj, baos);
		return baos.toByteArray();
	}

	public static Object deserialize(InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException(
					"The InputStream must not be null");
		}
		Kryo kryo = new Kryo();
		Input input = new Input(inputStream);
		Object object = kryo.readClassAndObject(input);
		input.close();
		return object;
	}

	public static Object deserialize(byte[] objectData) {
		if (objectData == null) {
			throw new IllegalArgumentException("The byte[] must not be null");
		}
		return deserialize(new ByteArrayInputStream(objectData));
	}

	public static void serializeObject(Object obj, OutputStream outputStream) {
		if (outputStream == null) {
			throw new IllegalArgumentException(
					"The OutputStream must not be null");
		}
		Kryo kryo = new Kryo();
		Output output = new Output(outputStream);
		kryo.writeObject(output, obj);
		output.close();
	}

	public static byte[] serializeObject(Object obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		serializeObject(obj, baos);
		return baos.toByteArray();
	}

	public static <T> T deserializeObject(InputStream inputStream, Class<T> clz) {
		if (inputStream == null) {
			throw new IllegalArgumentException(
					"The InputStream must not be null");
		}
		Kryo kryo = new Kryo();
		Input input = new Input(inputStream);
		T object = (T) kryo.readObject(input, clz);
		input.close();
		return object;
	}

	public static <T> T deserializeObject(byte[] objectData, Class<T> clz) {
		if (objectData == null) {
			throw new IllegalArgumentException("The byte[] must not be null");
		}
		return (T) deserializeObject(new ByteArrayInputStream(objectData), clz);
	}

}