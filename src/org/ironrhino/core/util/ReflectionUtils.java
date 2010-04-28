package org.ironrhino.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtils {

	public static Class getGenericClass(Class clazz) {
		return getGenericClass(clazz, 0);
	}

	public static Class getGenericClass(Class clazz, int index) {
		Type genType = clazz.getGenericSuperclass();
		if (genType instanceof ParameterizedType) {
			ParameterizedType pramType = (ParameterizedType) genType;
			Type[] params = pramType.getActualTypeArguments();
			if ((params != null) && (params.length > index)) {
				return (Class) params[index];
			}
		}
		return null;
	}

}
