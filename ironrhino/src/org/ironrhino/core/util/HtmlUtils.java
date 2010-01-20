package org.ironrhino.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

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
		StringBuilder sb = new StringBuilder();
		final List<String> ids = Arrays.asList(id);
		Source source = new Source(html);
		Iterator<Element> it = source.getAllElements().iterator();
		while (it.hasNext()) {
			Element element = it.next();
			String tid = element.getAttributeValue("id");
			if (tid != null && ids.contains(tid))
				sb.append(element.toString());
		}
		return sb.toString();
	}

}
