package org.ironrhino.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeIterator;
import org.ironrhino.core.model.BaseTreeableEntity;

public class HtmlUtils {

	public static String getTreeViewHtml(Collection children, boolean async) {
		if (async || children == null || children.size() == 0)
			return "<ul id=\"treeview\"></ul>";
		StringBuilder sb = new StringBuilder();
		sb.append("<ul id=\"treeview\">");
		for (Object c : children)
			sb.append(getTreeViewHtml((BaseTreeableEntity) c));
		sb.append("</ul>");
		return sb.toString();
	}

	public static String getTreeViewHtml(BaseTreeableEntity t) {
		StringBuilder sb = new StringBuilder();
		sb.append("<li id=\"" + t.getId() + "\"><span>" + t.getName()
				+ "</span>");
		if (t.getChildren().size() > 0) {
			sb.append("<ul>");
			for (Object c : t.getChildren())
				sb.append(getTreeViewHtml((BaseTreeableEntity) c));
			sb.append("</ul>");
		}
		sb.append("</li>");
		return sb.toString();
	}

	public static String compress(String[] id, String html) throws Exception {
		final List<String> ids = Arrays.asList(id);
		final Map<String, String> map = new LinkedHashMap<String, String>();
		Parser.createParser(html, "UTF-8").parse(new NodeFilter() {
			private static final long serialVersionUID = 5669597261933421020L;

			public boolean accept(Node node) {
				if (node instanceof Tag) {
					Tag tag = (Tag) node;
					if (!tag.isEndTag()) {
						String id = tag.getAttribute("id");
						if (ids.contains(id)) {
							String _html = tag.toHtml();
							map.put(id, _html.substring(_html.indexOf('>') + 1,
									_html.lastIndexOf('<')).trim());
							return true;
						}
					}
				}
				return false;
			}
		});
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append("<div id=\"");
			sb.append(entry.getKey());
			sb.append("\">");
			sb.append(entry.getValue());
			sb.append("</div>");
		}
		return sb.toString();
	}

	public static String tidy(String src) {
		try {
			StringBuilder sb = new StringBuilder();
			String head = "<div>";
			String foot = "</div>";
			sb.append(head);
			sb.append(src);
			sb.append(foot);
			Parser p = Parser.createParser(sb.toString(), "UTF-8");
			NodeIterator nl = p.elements();
			if (nl.hasMoreNodes()) {
				Node node = nl.nextNode();
				escapeHtml(node);
				src = node.toHtml();
				src = src
						.substring(head.length(), src.length() - foot.length());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return src;
	}

	private static void escapeHtml(Node node) {
		if (node instanceof Text) {
			node.setText(StringEscapeUtils.escapeHtml(node.getText()));
		} else if (node instanceof TagNode) {
			Vector<Attribute> attrs = ((TagNode) node).getAttributesEx();
			for (Attribute attr : attrs) {
				String text = attr.getRawValue();
				if (org.apache.commons.lang.StringUtils.isNotBlank(text)) {
					text = text.replaceAll("&nbsp;", "&");
					text = text.replaceAll("&", "&nbsp;");
					attr.setRawValue(text);
				}
			}
		}
		if (node.getChildren() != null && node.getChildren().size() > 0)
			for (int i = 0; i < node.getChildren().size(); i++)
				escapeHtml(node.getChildren().elementAt(i));

	}

}
