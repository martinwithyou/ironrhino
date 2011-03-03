package org.ironrhino.core.util;

import java.io.Reader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.compass.core.util.reader.StringReader;
import org.xml.sax.InputSource;

public class XmlUtils {

	private static XPath _xpath = XPathFactory.newInstance().newXPath();

	public static String eval(String xpath, Reader reader) {
		try {
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

}
