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
		return sql;
	}

	public static String trimComments(String sql) {
		return trimLineComments(trimBlockComments(sql));
	}

	private static String trimBlockComments(String sql) {
		return sql;
	}

	private static String trimLineComments(String sql) {
		return sql;
	}

	public static Set<String> extractParameters(String sql) {
		Set<String> names = new LinkedHashSet<String>();
		Matcher m = PARAMETER_PATTERN.matcher(sql);
		while (m.find())
			names.add(m.group().substring(1));
		return names;
	}

	public static Set<String> extractTables(String sql) {
		return extractTables(sql, "\"");
	}

	public static Set<String> extractTables(String sql, String quoteString) {
		Pattern tablePattern = Pattern.compile("from\\s+([\\w\\." + quoteString
				+ ",\\s]+)", Pattern.CASE_INSENSITIVE);
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

}
