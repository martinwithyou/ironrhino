package org.ironrhino.core.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanUtils;

public class AnnotationUtils {

	private static Map<String, Object> cache = new ConcurrentHashMap<String, Object>();

	public static Method getAnnotatedMethod(Class clazz,
			Class<? extends Annotation> annotaionClass) {
		Set<Method> set = getAnnotatedMethods(clazz, annotaionClass);
		if (set != null)
			return set.iterator().next();
		return null;
	}

	public static Set<Method> getAnnotatedMethods(Class clazz,
			Class<? extends Annotation> annotaionClass) {
		String key = clazz.getCanonicalName() + "-"
				+ annotaionClass.getCanonicalName();
		if (cache.get(key) == null) {
			Set<Method> set = new HashSet<Method>();
			try {
				Method[] methods = clazz.getMethods();
				for (Method m : methods)
					if (m.getAnnotation(annotaionClass) != null)
						set.add(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
			cache.put(key, set);
		}
		return (Set<Method>) cache.get(key);
	}

	public static Set<String> getAnnotatedPropertyNames(Class clazz,
			Class<? extends Annotation> annotaionClass) {
		String key = clazz.getCanonicalName() + "-"
				+ annotaionClass.getCanonicalName();
		if (cache.get(key) == null) {
			Set<String> set = new HashSet<String>();
			try {
				Field[] fs = clazz.getDeclaredFields();
				for (Field f : fs)
					if (f.getAnnotation(annotaionClass) != null)
						set.add(f.getName());
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

	public static Map<String, Object> getAnnotatedPropertyNameAndValues(
			Object object, Class<? extends Annotation> annotaionClass) {
		Set<String> propertyNames = getAnnotatedPropertyNames(
				object.getClass(), annotaionClass);
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			for (String key : propertyNames) {
				map.put(key, BeanUtils.getProperty(object, key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static Map<String, Annotation> getAnnotatedPropertyNameAndAnnotations(
			Class clazz, Class<? extends Annotation> annotaionClass) {
		String key = clazz.getCanonicalName() + ":"
				+ annotaionClass.getCanonicalName();
		Map<String, Annotation> map = new HashMap<String, Annotation>();
		if (cache.get(key) == null) {
			try {
				Field[] fs = clazz.getDeclaredFields();
				for (Field f : fs)
					if (f.getAnnotation(annotaionClass) != null)
						map.put(f.getName(), f.getAnnotation(annotaionClass));
				PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz)
						.getPropertyDescriptors();
				for (PropertyDescriptor pd : pds)
					if (pd.getReadMethod() != null
							&& pd.getReadMethod().getAnnotation(annotaionClass) != null)
						map.put(pd.getName(), pd.getReadMethod().getAnnotation(
								annotaionClass));
			} catch (Exception e) {
				e.printStackTrace();
			}
			cache.put(key, map);
		}
		return (Map<String, Annotation>) cache.get(key);
	}

}
