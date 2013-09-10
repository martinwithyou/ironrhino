package org.ironrhino.core.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.Order;

@SuppressWarnings("unchecked")
public class AnnotationUtils {

	private static Map<String, Object> cache = new ConcurrentHashMap<String, Object>(
			256);

	private static ValueThenKeyComparator<Method, Integer> comparator = new ValueThenKeyComparator<Method, Integer>() {
		@Override
		protected int compareKey(Method a, Method b) {
			return a.getName().compareTo(b.getName());
		}
	};

	public static Method getAnnotatedMethod(Class<?> clazz,
			Class<? extends Annotation> annotaionClass) {
		Iterator<Method> it = getAnnotatedMethods(clazz, annotaionClass)
				.iterator();
		if (it.hasNext())
			return it.next();
		return null;
	}

	public static Set<Method> getAnnotatedMethods(Class<?> clazz,
			Class<? extends Annotation> annotaionClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("getAnnotatedMethods:");
		sb.append(clazz.getName());
		sb.append(',');
		sb.append(annotaionClass.getName());
		String key = sb.toString();
		if (!cache.containsKey(key)) {
			final Map<Method, Integer> map = new HashMap<Method, Integer>();
			try {
				Method[] methods = clazz.getMethods();
				for (Method m : methods)
					if (m.getAnnotation(annotaionClass) != null) {
						Order o = m.getAnnotation(Order.class);
						map.put(m, o != null ? o.value() : 0);
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<Map.Entry<Method, Integer>> list = new ArrayList<Map.Entry<Method, Integer>>(
					map.entrySet());
			Collections.sort(list, comparator);
			Set<Method> methods = new LinkedHashSet<Method>();
			for (Map.Entry<Method, Integer> entry : list)
				methods.add(entry.getKey());
			cache.put(key, methods);
		}
		return (Set<Method>) cache.get(key);
	}

	public static Set<String> getAnnotatedPropertyNames(Class<?> clazz,
			Class<? extends Annotation> annotaionClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("getAnnotatedPropertyNames:");
		sb.append(clazz.getName());
		sb.append(',');
		sb.append(annotaionClass.getName());
		String key = sb.toString();
		if (!cache.containsKey(key)) {
			Set<String> set = new HashSet<String>();
			try {
				Class<?> cls = clazz;
				while (!cls.equals(Object.class)) {
					Field[] fs = cls.getDeclaredFields();
					for (Field f : fs)
						if (f.getAnnotation(annotaionClass) != null)
							set.add(f.getName());
					cls = cls.getSuperclass();
				}
				PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz)
						.getPropertyDescriptors();
				for (PropertyDescriptor pd : pds)
					if (pd.getReadMethod() != null
							&& pd.getReadMethod().getAnnotation(annotaionClass) != null)
						set.add(pd.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			cache.put(key, set);
		}
		return (Set<String>) cache.get(key);
	}

	@SafeVarargs
	public static Map<String, Object> getAnnotatedPropertyNameAndValues(
			Object object, Class<? extends Annotation>... annotaionClass) {
		if (annotaionClass.length == 0)
			return Collections.emptyMap();
		Map<String, Object> map = new HashMap<String, Object>();
		Set<String> propertyNames = new HashSet<String>();
		for (Class<? extends Annotation> clz : annotaionClass)
			propertyNames.addAll(getAnnotatedPropertyNames(object.getClass(),
					clz));
		BeanWrapperImpl bw = new BeanWrapperImpl(object);
		try {
			for (String key : propertyNames) {
				map.put(key, bw.getPropertyValue(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static <T extends Annotation> Map<String, T> getAnnotatedPropertyNameAndAnnotations(
			Class<?> clazz, Class<T> annotaionClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("getAnnotatedPropertyNameAndAnnotations:");
		sb.append(clazz.getName());
		sb.append(',');
		sb.append(annotaionClass.getName());
		String key = sb.toString();
		if (!cache.containsKey(key)) {
			Map<String, T> map = new HashMap<String, T>();
			try {
				Class<?> cls = clazz;
				while (!cls.equals(Object.class)) {
					Field[] fs = cls.getDeclaredFields();
					for (Field f : fs)
						if (f.getAnnotation(annotaionClass) != null)
							map.put(f.getName(),
									f.getAnnotation(annotaionClass));
					cls = cls.getSuperclass();
				}
				PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz)
						.getPropertyDescriptors();
				for (PropertyDescriptor pd : pds)
					if (pd.getReadMethod() != null
							&& pd.getReadMethod().getAnnotation(annotaionClass) != null)
						map.put(pd.getName(),
								pd.getReadMethod()
										.getAnnotation(annotaionClass));
			} catch (Exception e) {
				e.printStackTrace();
			}
			cache.put(key, map);
		}
		return (Map<String, T>) cache.get(key);
	}

	public static <T extends Annotation> T getAnnotation(Class<?> clazz,
			Class<T> annotationClass, String methodName, Class<?>... paramTypes) {
		StringBuilder sb = new StringBuilder();
		sb.append("getAnnotation:");
		sb.append(clazz.getName());
		sb.append(',');
		sb.append(annotationClass.getName());
		sb.append(',');
		sb.append(methodName);
		if (paramTypes.length > 0) {
			sb.append('(');
			sb.append(StringUtils.join(paramTypes, ","));
			sb.append(')');
		}
		String key = sb.toString();
		if (!cache.containsKey(key)) {
			Method method = org.springframework.beans.BeanUtils.findMethod(
					clazz, methodName, paramTypes);
			Object annotation = method != null ? method
					.getAnnotation(annotationClass) : null;
			if (annotation == null)
				annotation = "null";
			cache.put(key, annotation);
		}
		Object v = cache.get(key);
		if (v instanceof Annotation)
			return (T) v;
		return null;
	}

	public static <T extends Annotation> T getAnnotation(Class<?> clazz,
			Class<T> annotationClass) {
		T annotation = null;
		Class<?> c = clazz;
		while (annotation == null && c != null) {
			annotation = clazz.getAnnotation(annotationClass);
			c = clazz.getSuperclass();
			if (c == null || c.getClass().equals(Object.class))
				break;
		}
		return annotation;
	}

}
