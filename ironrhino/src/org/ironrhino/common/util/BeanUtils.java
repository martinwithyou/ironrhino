package org.ironrhino.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.springframework.aop.framework.ProxyFactory;

public class BeanUtils {

	private static final Log log = LogFactory.getLog(BeanUtils.class);

	public static boolean hasProperty(Class clazz, String name) {
		if (org.springframework.beans.BeanUtils.getPropertyDescriptor(clazz,
				name) != null)
			return true;
		return false;
	}

	public static void copyProperties(Object source, Object target,
			String... ignoreProperties) {
		copyProperties(source, target, false, ignoreProperties);
	}

	public static EditAware copyProperties(Object source, Object target,
			boolean editAware, String... ignoreProperties) {
		ProxyFactory pc;
		if (editAware) {
			pc = new ProxyFactory();
			pc.setProxyTargetClass(true);
			pc.setTargetClass(target.getClass());
			pc.addAdvice(new EditAwareMixin());
			pc.setTarget(target);
			target = pc.getProxy();
		}
		Set<String> ignores = new HashSet<String>();
		ignores.addAll(AnnotationUtils.getAnnotatedPropertyNames(source
				.getClass(), NotInCopy.class));
		ignores.addAll(Arrays.asList(ignoreProperties));
		org.springframework.beans.BeanUtils.copyProperties(source, target,
				ignores.toArray(ignoreProperties));
		if (editAware)
			return (EditAware) target;
		return null;
	}

	public static <T extends BaseTreeableEntity<T>> T deepClone(T source,
			String... ignoreProperties) {
		return deepClone(source, null, ignoreProperties);
	}

	public static <T extends BaseTreeableEntity<T>> T deepClone(T source,
			ObjectFilter filter, String... ignoreProperties) {
		if (filter != null && !filter.accept(source))
			throw new IllegalArgumentException(
					"source object self must be accepted if you specify a filter");
		try {
			T ret = (T) source.getClass().newInstance();
			copyProperties(source, ret, ignoreProperties);
			List<T> children = new ArrayList<T>();
			for (T child : source.getChildren()) {
				if (filter == null || filter.accept(child)) {
					T t = deepClone(child, filter, ignoreProperties);
					t.setParent(ret);
					children.add(t);
				}
			}
			ret.setChildren(children);
			return ret;
		} catch (Exception e) {
			log.error("exception occurs", e);
			return null;
		}
	}

}
