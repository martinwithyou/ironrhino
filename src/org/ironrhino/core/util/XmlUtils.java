package org.ironrhino.core.util;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtils {
        public static void removeChildren(Node node) {
                NodeList childNodes = node.getChildNodes();
                int length = childNodes.getLength();
                for (int i = length - 1; i > -1; i--)
                        node.removeChild(childNodes.item(i));
        }

        public static Document loadDocument(String file)
                        throws ParserConfigurationException, SAXException, IOException {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                return builder.parse(file);
        }

        public static void saveDocument(Document dom, String file)
                        throws TransformerException, IOException {

                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();

                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, dom
                                .getDoctype().getPublicId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dom
                                .getDoctype().getSystemId());

                DOMSource source = new DOMSource(dom);
                StreamResult result = new StreamResult();

                FileOutputStream outputStream = new FileOutputStream(file);
                result.setOutputStream(outputStream);
                transformer.transform(source, result);

                outputStream.flush();
                outputStream.close();
        }
}