package org.ironrhino.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

public class ReflectionUtils {

	protected static ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

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

	public static String[] getParameterNames(Constructor ctor) {
		return parameterNameDiscoverer.getParameterNames(ctor);
	}

	public static String[] getParameterNames(Method method) {
		return parameterNameDiscoverer.getParameterNames(method);
	}

	public static String[] getParameterNames(JoinPoint jp) {
		if (!jp.getKind().equals(JoinPoint.METHOD_EXECUTION))
			return null;
		Class<?> clz = jp.getTarget().getClass();
		MethodSignature sig = (MethodSignature) jp.getSignature();
		Method method;
		try {
			method = clz.getDeclaredMethod(sig.getName(),
					sig.getParameterTypes());
			return parameterNameDiscoverer.getParameterNames(method);
		} catch (Exception e) {
			return null;
		}
	}

}
