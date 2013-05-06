package org.ironrhino.core.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamUtils {

	private static XStream xstream = new XStream(new DomDriver("utf-8"));

	static {
		xstream.registerConverter(new DateConverter(
				"EEE MMM dd HH:mm:ss zzz yyyy", new String[] {
						"yyyy-MM-dd HH:mm:ss", "yyyy-MM-ddTHH:mm:ss",
						"yyyy-MM-dd HH:mm:ss.SSS" }));
	}

	public static String toXml(Object obj) {
		xstream.processAnnotations(obj.getClass());
		String result = xstream.toXML(obj);
		if (result.indexOf("<?xml") != 0)
			result = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" + result;
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromXml(String xml, Class<T> clazz) {
		xstream.processAnnotations(clazz);
		return (T) xstream.fromXML(xml);
	}

}
