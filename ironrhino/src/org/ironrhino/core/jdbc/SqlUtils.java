package org.ironrhino.core.jdbc;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtils {

	public static String refineSql(String sql) {
		sql = sql.trim();
		if (sql.endsWith(";"))
			sql = sql.substring(0, sql.length() - 1);
		return new StringBuilder(sql.length() + 2).append("\n").append(sql)
				.append("\n").toString();
	}

	public static String trimComments(String sql) {
		return trimLineComments(trimBlockComments(sql));
	}

	private static String trimBlockComments(String sql) {
		if (sql.indexOf("/*") > -1)
			sql = BLOCK_COMMENTS_PATTERN.matcher(sql).replaceAll("");
		return sql;
	}

	private static String trimLineComments(String sql) {
		if (sql.indexOf("--") > -1)
			sql = LINE_COMMENTS_PATTERN.matcher(sql).replaceAll("\n").replaceAll("\n+", "\n").trim();
		return sql;
	}

	public static Set<String> extractParameters(String sql) {
		sql = trimComments(sql);
		Set<String> names = new LinkedHashSet<String>();
		Matcher m = PARAMETER_PATTERN.matcher(sql);
		while (m.find())
			names.add(m.group().substring(1));
		return names;
	}

	public static Set<String> extractTables(String sql, String quoteString) {
		return extractTables(sql, quoteString, "from");
	}

	public static Set<String> extractTables(String sql, String quoteString,
			String frontKeyword) {
		sql = trimComments(sql);
		Pattern tablePattern = Pattern.compile(frontKeyword + "\\s+([\\w\\."
				+ quoteString + ",\\s]+)", Pattern.CASE_INSENSITIVE);
		Set<String> names = new LinkedHashSet<String>();
		Matcher m = tablePattern.matcher(sql);
		while (m.find()) {
			String arr[] = m.group(1).split(",");
			for (String s : arr) {
				names.add(s.trim().split("\\s+")[0]);
			}
		}
		return names;
	}

	private static final Pattern PARAMETER_PATTERN = Pattern.compile(
			"(:[a-z]\\w*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern BLOCK_COMMENTS_PATTERN = Pattern
			.compile("/\\*(?:.|[\\n\\r])*?\\*/");

	private static final Pattern LINE_COMMENTS_PATTERN = Pattern
			.compile("\r?\n?\\s*--.*\r?(\n|$)");


}
