package org.ironrhino.core.ext.spring;

import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.context.ApplicationContext;

public abstract class AbstractApplicationContextConsole implements
		ApplicationContextConsole {

	protected Log log = LogFactory.getLog(getClass());

	@Autowired
	private ApplicationContext ctx;

	private BeanWrapper bw;

	private PropertyEditor findPropertyEditor(Class type) throws Exception {
		if (bw == null) {
			bw = new BeanWrapperImpl();
			registerCustomEditors(bw);
		}
		return bw.findCustomEditor(type, null);
	}

	private void registerCustomEditors(BeanWrapper bw) {
		bw
				.registerCustomEditor(String[].class,
						new StringArrayPropertyEditor());
		AbstractBeanFactory bf = (AbstractBeanFactory) ctx
				.getAutowireCapableBeanFactory();
		for (Iterator it = bf.getPropertyEditorRegistrars().iterator(); it
				.hasNext();) {
			PropertyEditorRegistrar registrar = (PropertyEditorRegistrar) it
					.next();
			registrar.registerCustomEditors(bw);
		}
		for (Iterator it = bf.getCustomEditors().keySet().iterator(); it
				.hasNext();) {
			Class clazz = (Class) it.next();
			PropertyEditor editor = (PropertyEditor) bf.getCustomEditors().get(
					clazz);
			bw.registerCustomEditor(clazz, editor);
		}
	}

	public abstract Object execute(String cmd) throws Exception;

	public void set(String path, String value) throws Exception {
		if (path == null || path.indexOf('.') < 0)
			throw new IllegalArgumentException("no path:" + path);
		String beanPath = path.substring(0, path.lastIndexOf('.'));
		String propertyName = path.substring(path.lastIndexOf('.') + 1);
		Object bean = get(beanPath);
		if (bean == null)
			throw new IllegalArgumentException("no path:" + path);
		BeanWrapper bw = new BeanWrapperImpl(bean);
		registerCustomEditors(bw);
		bw.setPropertyValue(propertyName, value);
	}

	public Object get(String path) throws Exception {
		if (path == null)
			throw new IllegalArgumentException("no path:" + path);
		Object result;
		if (path.indexOf('.') < 0) {
			result = ctx.getBean(path);
		} else {
			String beanName = path.substring(0, path.indexOf('.'));
			String propertyPath = path.substring(path.indexOf('.') + 1);
			Object bean = ctx.getBean(beanName);
			result = PropertyUtils.getProperty(bean, propertyPath);
		}
		if (result == null)
			throw new IllegalArgumentException("no path:" + path);
		return result;
	}

	public Object call(String path, String[] params) throws Exception {
		if (path == null)
			throw new IllegalArgumentException("no path for command:'call'");
		int sep = 0;
		if (path.indexOf('(') > 0) {
			String temp = path.substring(0, path.indexOf('('));
			sep = temp.lastIndexOf('.');
		} else {
			sep = path.lastIndexOf('.');
		}
		String targetName = path.substring(0, sep);
		String signature = path.substring(sep + 1);
		Object target = get(targetName);
		return doCall(target, signature, params);
	}

	public Object callWithMap(String path, Map<String, String> properties)
			throws Exception {
		if (path == null)
			throw new IllegalArgumentException("no path for command:'call'");
		int sep = 0;
		if (path.indexOf('(') > 0) {
			String temp = path.substring(0, path.indexOf('('));
			sep = temp.lastIndexOf('.');
		} else {
			sep = path.lastIndexOf('.');
		}
		String targetName = path.substring(0, sep);
		String signature = path.substring(sep + 1);
		Object target = get(targetName);
		Method method = BeanUtils
				.resolveSignature(signature, target.getClass());
		Class paramType = method.getParameterTypes()[0];
		Object paramValue = paramType.newInstance();
		BeanWrapperImpl bw = new BeanWrapperImpl(paramValue);
		registerCustomEditors(bw);
		bw.setPropertyValues(properties);
		return method.invoke(target, paramValue);
	}

	private Object doCall(Object target, String signature, String... params)
			throws Exception {
		if (target == null)
			throw new IllegalArgumentException("target cannot be null");
		Method method = BeanUtils
				.resolveSignature(signature, target.getClass());
		if (method == null)
			throw new IllegalArgumentException("no method:" + signature
					+ " for class:" + target.getClass().getName());
		if (!signature.endsWith(")") || signature.endsWith("()"))
			return method.invoke(target, new Object[] {});
		String[] paramType = signature.substring(signature.indexOf("(") + 1,
				signature.indexOf(")")).split(",");
		if (params.length != paramType.length)
			throw new IllegalArgumentException("you have to specify "
					+ paramType.length + " args");
		Object[] paramValue = new Object[paramType.length];
		for (int i = 0; i < paramType.length; i++) {
			PropertyEditor pe = null;
			Class type = null;
			if (paramType[i].endsWith("[]")) {
				Class cla = Class.forName(paramType[i].substring(0,
						paramType[i].length() - 2));
				type = Array.newInstance(cla, 0).getClass();
			} else {
				type = Class.forName(paramType[i]);
			}
			pe = findPropertyEditor(type);
			if (pe == null)
				throw new IllegalArgumentException(
						"no PropertyEditor for class:" + type.getName()
								+ " configured in application context");
			pe.setAsText(params[i]);
			paramValue[i] = pe.getValue();
		}
		return method.invoke(target, paramValue);
	}

}