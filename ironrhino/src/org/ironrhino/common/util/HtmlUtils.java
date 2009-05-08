package org.ironrhino.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeIterator;
import org.ironrhino.core.model.BaseTreeableEntity;

public class HtmlUtils {
	static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
			"<script[^>]*>.*</script[^>]*>", Pattern.CASE_INSENSITIVE);

	static final PatternCompiler pc = new Perl5Compiler();
	static final PatternMatcher matcher = new Perl5Matcher();

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

	public static void parseInnerHTML(final Map<String, String> map, String html)
			throws Exception {
		Parser.createParser(html, "UTF-8").parse(new NodeFilter() {
			public boolean accept(Node node) {
				if (node instanceof Tag) {
					Tag tag = (Tag) node;
					if (!tag.isEndTag()) {
						String id = tag.getAttribute("id");
						if (map.containsKey(id)) {
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
	}

	public static String tidy(String src) throws Exception {
		StringBuilder sb = new StringBuilder();
		String head = "<div>";
		String foot = "</div>";
		sb.append(head);
		sb.append(src);
		sb.append(foot);
		Parser p = Parser.createParser(sb.toString(), "UTF-8");
		NodeIterator nl = p.elements();
		if(nl.hasMoreNodes()){
			src = nl.nextNode().toHtml();
			src = src.substring(head.length(),src.length()-foot.length());
		}
		return src;
	}

	// http://www.bitscn.com/hack/young/200708/108278.html
	public static String antiXSS(String content) {
		String old = content;
		String ret = _antiXSS(content);
		while (!ret.equals(old)) {
			old = ret;
			ret = _antiXSS(ret);
		}
		return ret;
	}

	public static boolean hasXSS(String content) {
		try {
			return hasScriptTag(content) || hasEvent(content)
					|| hasAsciiAndHex(content) || hasCssExpression(content)
					|| hasProtocol(content) || hasAllowScriptAccess(content);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private static String _antiXSS(String content) {
		try {
			return stripAllowScriptAccess(stripProtocol(stripCssExpression(stripAsciiAndHex(stripEvent(stripScriptTag(content))))));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String stripScriptTag(String content) {
		Matcher m = SCRIPT_TAG_PATTERN.matcher(content);
		content = m.replaceAll("");
		return content;
	}

	private static boolean hasScriptTag(String content) {
		Matcher m = SCRIPT_TAG_PATTERN.matcher(content);
		return m.matches();
	}

	private static String stripEvent(String content) throws Exception {
		String[] events = { "onmouseover", "onmouseout", "onmousedown",
				"onmouseup", "onmousemove", "onclick", "ondblclick",
				"onkeypress", "onkeydown", "onkeyup", "ondragstart",
				"onerrorupdate", "onhelp", "onreadystatechange", "onrowenter",
				"onrowexit", "onselectstart", "onload", "onunload",
				"onbeforeunload", "onblur", "onerror", "onfocus", "onresize",
				"onscroll", "oncontextmenu" };
		for (String event : events) {
			org.apache.oro.text.regex.Pattern p = pc.compile("(<[^>]*)("
					+ event + ")([^>]*>)", Perl5Compiler.CASE_INSENSITIVE_MASK);
			if (null != p)
				content = Util.substitute(matcher, p, new Perl5Substitution(
						"$1" + event.substring(2) + "$3"), content,
						Util.SUBSTITUTE_ALL);

		}
		return content;
	}

	private static boolean hasEvent(String content) throws Exception {
		String[] events = { "onmouseover", "onmouseout", "onmousedown",
				"onmouseup", "onmousemove", "onclick", "ondblclick",
				"onkeypress", "onkeydown", "onkeyup", "ondragstart",
				"onerrorupdate", "onhelp", "onreadystatechange", "onrowenter",
				"onrowexit", "onselectstart", "onload", "onunload",
				"onbeforeunload", "onblur", "onerror", "onfocus", "onresize",
				"onscroll", "oncontextmenu" };
		for (String event : events) {
			org.apache.oro.text.regex.Pattern p = pc.compile("(<[^>]*)("
					+ event + ")([^>]*>)", Perl5Compiler.CASE_INSENSITIVE_MASK);
			if (null != p)
				return true;
		}
		return false;
	}

	private static String stripAsciiAndHex(String content) throws Exception {
		org.apache.oro.text.regex.Pattern p = pc.compile(
				"(<[^>]*)(&#|\\\\00)([^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util
					.substitute(matcher, p, new Perl5Substitution("$1$3"),
							content, Util.SUBSTITUTE_ALL);
		return content;
	}

	private static boolean hasAsciiAndHex(String content) throws Exception {
		org.apache.oro.text.regex.Pattern p = pc.compile(
				"(<[^>]*)(&#|\\\\00)([^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		return p != null;
	}

	private static String stripCssExpression(String content) throws Exception {
		org.apache.oro.text.regex.Pattern p = pc.compile(
				"(<[^>]*style=.*)/\\*.*\\*/([^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util
					.substitute(matcher, p, new Perl5Substitution("$1$2"),
							content, Util.SUBSTITUTE_ALL);

		p = pc
				.compile(
						"(<[^>]*style=[^>]+)(expression|javascript|vbscript|-moz-binding)([^>]*>)",
						Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util
					.substitute(matcher, p, new Perl5Substitution("$1$3"),
							content, Util.SUBSTITUTE_ALL);

		p = pc.compile("(<style[^>]*>.*)/\\*.*\\*/(.*</style[^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util
					.substitute(matcher, p, new Perl5Substitution("$1$2"),
							content, Util.SUBSTITUTE_ALL);

		p = pc
				.compile(
						"(<style[^>]*>[^>]+)(expression|javascript|vbscript|-moz-binding)(.*</style[^>]*>)",
						Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util
					.substitute(matcher, p, new Perl5Substitution("$1$3"),
							content, Util.SUBSTITUTE_ALL);
		return content;
	}

	private static boolean hasCssExpression(String content) throws Exception {
		org.apache.oro.text.regex.Pattern p = pc.compile(
				"(<[^>]*style=.*)/\\*.*\\*/([^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util
					.substitute(matcher, p, new Perl5Substitution("$1$2"),
							content, Util.SUBSTITUTE_ALL);

		p = pc
				.compile(
						"(<[^>]*style=[^>]+)(expression|javascript|vbscript|-moz-binding)([^>]*>)",
						Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			return true;
		p = pc.compile("(<style[^>]*>.*)/\\*.*\\*/(.*</style[^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util
					.substitute(matcher, p, new Perl5Substitution("$1$2"),
							content, Util.SUBSTITUTE_ALL);
		p = pc
				.compile(
						"(<style[^>]*>[^>]+)(expression|javascript|vbscript|-moz-binding)(.*</style[^>]*>)",
						Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			return true;
		return false;
	}

	private static String stripProtocol(String content) throws Exception {
		String[] protocols = { "javascript", "vbscript", "livescript",
				"ms-its", "mhtml", "data", "firefoxurl", "mocha" };
		for (String protocol : protocols) {
			org.apache.oro.text.regex.Pattern p = pc.compile("(<[^>]*)"
					+ protocol + ":([^>]*>)",
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			if (null != p)
				content = Util.substitute(matcher, p, new Perl5Substitution(
						"$1/$2"), content, Util.SUBSTITUTE_ALL);
		}
		return content;
	}

	private static boolean hasProtocol(String content) throws Exception {
		String[] protocols = { "javascript", "vbscript", "livescript",
				"ms-its", "mhtml", "data", "firefoxurl", "mocha" };
		for (String protocol : protocols) {
			org.apache.oro.text.regex.Pattern p = pc.compile("(<[^>]*)"
					+ protocol + ":([^>]*>)",
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			if (null != p)
				return true;
		}
		return false;
	}

	private static String stripAllowScriptAccess(String content)
			throws Exception {
		org.apache.oro.text.regex.Pattern p = pc.compile(
				"(<[^>]*)AllowScriptAccess([^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		if (null != p)
			content = Util.substitute(matcher, p, new Perl5Substitution(
					"$1Allow_Script_Access$2"), content, Util.SUBSTITUTE_ALL);
		return content;
	}

	private static boolean hasAllowScriptAccess(String content)
			throws Exception {
		org.apache.oro.text.regex.Pattern p = pc.compile(
				"(<[^>]*)AllowScriptAccess([^>]*>)",
				Perl5Compiler.CASE_INSENSITIVE_MASK);
		return p != null;
	}

}