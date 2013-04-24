package org.ironrhino.core.util;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XmlUtils {

	private static DocumentBuilder documentBuilder;

	private static XPath _xpath = XPathFactory.newInstance().newXPath();

	private static XStream xstream = new XStream(new DomDriver("utf-8"));

	static {
		xstream.registerConverter(new DateConverter(
				"EEE MMM dd HH:mm:ss zzz yyyy", new String[] {
						"yyyy-MM-dd HH:mm:ss", "yyyy-MM-ddTHH:mm:ss",
						"yyyy-MM-dd HH:mm:ss.SSS" }));
	}

	public static DocumentBuilder getDocumentBuilder() {
		if (documentBuilder == null) {
			try {
				documentBuilder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		return documentBuilder;
	}

	public static String eval(String xpath, Reader reader) {
		try {
			XPathExpression xpe = _xpath.compile(xpath);
			return xpe.evaluate(new InputSource(reader));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String eval(String xpath, Reader reader,
			NamespaceContext nsContext) {
		try {
			XPath _xpath = XPathFactory.newInstance().newXPath();
			_xpath.setNamespaceContext(nsContext);
			XPathExpression xpe = _xpath.compile(xpath);
			return xpe.evaluate(new InputSource(reader));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String eval(String xpath, String source) {
		return eval(xpath, new StringReader(source));
	}

	public static String eval(String xpath, String source,
			NamespaceContext nsContext) {
		return eval(xpath, new StringReader(source), nsContext);
	}

	public static NodeList evalNodeList(String xpath, Reader reader) {
		try {
			XPathExpression xpe = _xpath.compile(xpath);
			return (NodeList) xpe.evaluate(new InputSource(reader),
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static NodeList evalNodeList(String xpath, Reader reader,
			NamespaceContext nsContext) {
		try {
			XPath _xpath = XPathFactory.newInstance().newXPath();
			_xpath.setNamespaceContext(nsContext);
			XPathExpression xpe = _xpath.compile(xpath);
			return (NodeList) xpe.evaluate(new InputSource(reader),
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static NodeList evalNodeList(String xpath, String source) {
		return evalNodeList(xpath, new StringReader(source));
	}

	public static NodeList evalNodeList(String xpath, String source,
			NamespaceContext nsContext) {
		return evalNodeList(xpath, new StringReader(source), nsContext);
	}

	public static String toXml(Object obj) {
		xstream.processAnnotations(obj.getClass());
		String result = xstream.toXML(obj);
		if (result.indexOf("<?xml") != 0)
			result = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> \n" + result;
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromXml(String xml, Class<T> clazz) {
		xstream.processAnnotations(clazz);
		return (T) xstream.fromXML(xml);
	}

}
