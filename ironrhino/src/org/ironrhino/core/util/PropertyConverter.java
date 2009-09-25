package org.ironrhino.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;

/* 
 * @author ajoo.javaeye.com 
 * @See http://www.javaeye.com/topic/190440 
 */
public final class PropertyConverter<T> {
	private final Class<T> targetType;

	private PropertyConverter(Class<T> targetType) {
		this.targetType = targetType;
	}

	public static <T> PropertyConverter<T> to(Class<T> targetType) {
		return new PropertyConverter<T>(targetType);

	}

	public T from(final Map<String, String> map) {
		return (T) Proxy.newProxyInstance(targetType.getClassLoader(),
				new Class[] { targetType }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) {
						String name = method.getName();
						Class type = method.getReturnType();
						if (name.startsWith("get"))
							name = Character.toLowerCase(name.charAt(3))
									+ name.substring(4);
						if (name.startsWith("is") && type == boolean.class)
							name = Character.toLowerCase(name.charAt(2))
									+ name.substring(3);
						String value = map.get(name);
						if (value == null && !type.isPrimitive())
							return method.getDefaultValue();
						if(Enum.class.isAssignableFrom(type))
							return Enum.valueOf(type, value);
						return ConvertUtils.convert(value, type);
					}
				});
	}
	
//	static enum Type{
//		A,B
//	}
//
//	 static @interface Test {
//	
//	 int getId();
//	
//	 boolean isEnabled();
//	
//	 String getName();
//	
//	 Type type();
//	
//	 }
//	
//	 public static void main(String... args) {
//	 Map<String, String> map = new HashMap<String, String>();
//	 map.put("name", "test");
//	 map.put("enabled", "true");
//	 map.put("type", "A");
//	 Test t = PropertyConverter.to(Test.class).from(map);
//	 System.out.println(t.type());
//	 BeanWrapper bw = new BeanWrapperImpl(t);
//	 System.out.println(bw.getPropertyValue("name"));
//	 System.out.println(bw.getPropertyValue("enabled"));
//	
//		}

}
