package org.ironrhino.core.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

@Singleton
@Named
public class JdbcQueryService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private JdbcTemplate jdbcTemplate;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private DatabaseProduct databaseProduct;

	@Value("${jdbcQueryService.restricted:true}")
	private boolean restricted = true;

	private String catalog;

	private String schema;

	private String quoteString = "\"";

	private List<String> keywords = new ArrayList<String>();

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setDatabaseProduct(DatabaseProduct databaseProduct) {
		this.databaseProduct = databaseProduct;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	@PostConstruct
	public void init() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(
				jdbcTemplate);

		Connection con = DataSourceUtils.getConnection(jdbcTemplate
				.getDataSource());
		try {
			catalog = con.getCatalog();
			try {
				schema = con.getSchema();
			} catch (Throwable t) {

			}
			DatabaseMetaData dbmd = con.getMetaData();
			if (databaseProduct == null)
				databaseProduct = DatabaseProduct.parse(dbmd
						.getDatabaseProductName());
			keywords = databaseProduct.getKeywords();
			String str = dbmd.getIdentifierQuoteString();
			if (StringUtils.isNotBlank(str))
				quoteString = str.trim().substring(0, 1);
			if (keywords.isEmpty()) {
				str = dbmd.getSQLKeywords();
				if (StringUtils.isNotBlank(str))
					keywords.addAll(Arrays.asList(str.toUpperCase().split(",")));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DataSourceUtils
					.releaseConnection(con, jdbcTemplate.getDataSource());
		}
		TABLE_PATTERN = Pattern.compile("from\\s+([\\w\\." + quoteString
				+ ",\\s]+)", Pattern.CASE_INSENSITIVE);
	}

	public List<String> getTables() {
		List<String> tables = new ArrayList<String>();
		Connection con = DataSourceUtils.getConnection(jdbcTemplate
				.getDataSource());
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet rs = dbmd.getTables(catalog, schema, "%",
					new String[] { "TABLE" });
			while (rs.next()) {
				String table = rs.getString(3);
				if (keywords.contains(table.toUpperCase()))
					table = new StringBuilder(table.length() + 2)
							.append(quoteString).append(table)
							.append(quoteString).toString();
				tables.add(table);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DataSourceUtils
					.releaseConnection(con, jdbcTemplate.getDataSource());
		}
		return tables;
	}

	public void validate(String sql) {
		sql = refineSql(sql);
		if (restricted) {
			for (String table : extractTables(sql)) {
				if (table.indexOf('.') < 0)
					continue;
				if (table.startsWith(quoteString)
						&& table.endsWith(quoteString)
						&& !table.substring(1, table.length() - 1).contains(
								quoteString))
					continue;
				String[] arr = table.split("\\.");
				if (arr.length == 2) {
					String prefix = arr[0].replaceAll(quoteString, "");
					if (!prefix.equalsIgnoreCase(catalog)
							&& !prefix.equalsIgnoreCase(schema)) {
						throw new ErrorMessage("query.access.denied",
								new Object[] { table });
					}
				} else if (arr.length > 2) {
					String prefix1 = arr[0].replaceAll(quoteString, "");
					String prefix2 = arr[1].replaceAll(quoteString, "");
					if (!prefix1.equalsIgnoreCase(catalog)
							&& !prefix2.equalsIgnoreCase(schema)) {
						throw new ErrorMessage("query.access.denied",
								new Object[] { table });
					}
				}
			}
		}
		Set<String> names = extractParameters(sql);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String name : names)
			paramMap.put(name, "0");
		validateAndConvertTypes(sql, paramMap);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validateAndConvertTypes(String sql, Map paramMap) {
		try {
			query(sql, paramMap, 1);
		} catch (BadSqlGrammarException bse) {
			Throwable t = bse.getCause();
			if (t.getClass().getSimpleName().equals("PSQLException")) {
				String error = t.getMessage().toLowerCase();
				if ((error.indexOf("smallint") > 0
						|| error.indexOf("bigint") > 0
						|| error.indexOf("bigserial") > 0
						|| error.indexOf("integer") > 0
						|| error.indexOf("serial") > 0
						|| error.indexOf("numeric") > 0
						|| error.indexOf("decimal") > 0
						|| error.indexOf("real") > 0
						|| error.indexOf("double precision") > 0
						|| error.indexOf("money") > 0
						|| error.indexOf("timestamp") > 0
						|| error.indexOf("date") > 0 || error.indexOf("time") > 0)
						&& error.indexOf("character varying") > 0
						&& error.indexOf("：") > 0) {
					int location = Integer.valueOf(error.substring(
							error.lastIndexOf("：") + 1).trim());
					String paramName = sql.substring(location);
					paramName = paramName.substring(paramName.indexOf(":") + 1);
					paramName = paramName.split("\\s")[0].split("\\)")[0];
					Object object = paramMap.get(paramName);
					if (object != null) {
						String value = object.toString();
						if (error.indexOf("small") > 0)
							paramMap.put(paramName, Short.valueOf(value));
						else if (error.indexOf("bigint") > 0
								|| error.indexOf("bigserial") > 0)
							paramMap.put(paramName, Long.valueOf(value));
						else if (error.indexOf("integer") > 0
								|| error.indexOf("serial") > 0)
							paramMap.put(paramName, Integer.valueOf(value));
						else if (error.indexOf("numeric") > 0
								|| error.indexOf("decimal") > 0
								|| error.indexOf("real") > 0
								|| error.indexOf("double precision") > 0
								|| error.indexOf("money") > 0)
							paramMap.put(paramName, new BigDecimal(value));
						else if (error.indexOf("timestamp") > 0
								|| error.indexOf("date") > 0
								|| error.indexOf("time") > 0)
							paramMap.put(
									paramName,
									value.equals("0") ? new Date() : DateUtils
											.parse(value));
						validateAndConvertTypes(sql, paramMap);
						return;
					}
				}
			}
			String cause = "";
			if (t instanceof SQLException)
				cause = t.getMessage();
			throw new ErrorMessage("query.bad.sql.grammar",
					new Object[] { cause });
		} catch (DataAccessException e) {
			throw new ErrorMessage(e.getMessage());
		}
	}

	public long count(String sql, Map<String, ?> paramMap) {
		sql = refineSql(sql);
		String alias = "tfc";
		while (sql.contains(alias))
			alias += "0";
		StringBuilder sb = new StringBuilder("select count(*) from (")
				.append(trimOrderby(sql)).append(") ").append(alias);
		return namedParameterJdbcTemplate.queryForObject(sb.toString(),
				paramMap, Long.class);
	}

	public List<Map<String, Object>> query(String sql, Map<String, ?> paramMap) {
		return namedParameterJdbcTemplate.queryForList(sql, paramMap);
	}

	public void query(String sql, Map<String, ?> paramMap,
			RowCallbackHandler rch) {
		namedParameterJdbcTemplate.query(sql, paramMap, rch);
	}

	public List<Map<String, Object>> query(String sql, Map<String, ?> paramMap,
			final int limit) {
		return query(sql, paramMap, limit, 0);
	}

	public List<Map<String, Object>> query(String sql, Map<String, ?> paramMap,
			final int limit, final int offset) {
		sql = refineSql(sql);
		if (hasLimit(sql))
			return namedParameterJdbcTemplate.queryForList(sql, paramMap);
		if (databaseProduct == DatabaseProduct.MYSQL
				|| databaseProduct == DatabaseProduct.POSTGRESQL
				|| databaseProduct == DatabaseProduct.H2) {
			StringBuilder sb = new StringBuilder(sql.length() + 20);
			sb.append(sql);
			sb.append(" limit ");
			sb.append(limit);
			if (offset > 0) {
				sb.append(" offset ");
				sb.append(offset);
			}
			return namedParameterJdbcTemplate.queryForList(sb.toString(),
					paramMap);
		} else if (databaseProduct == DatabaseProduct.HSQL) {
			StringBuilder sb = new StringBuilder(sql.length() + 20);
			sb.append(sql);
			if (offset > 0) {
				sb.append(" offset ");
				sb.append(offset);
			}
			sb.append(" limit ");
			sb.append(limit);
			return namedParameterJdbcTemplate.queryForList(sb.toString(),
					paramMap);
		} else if (databaseProduct == DatabaseProduct.ORACLE) {
			StringBuilder sb = new StringBuilder(sql.length() + 100);
			if (offset > 0) {
				sb.append("select * from ( select row_.*, rownum rownum_ from ( ");
			} else {
				sb.append("select * from ( ");
			}
			sb.append(sql);
			if (offset > 0) {
				sb.append(" ) row_ ) where rownum_ <= " + (limit + offset)
						+ " and rownum_ > " + offset);
			} else {
				sb.append(" ) where rownum <= " + limit);
			}
			List<Map<String, Object>> list = namedParameterJdbcTemplate
					.queryForList(sb.toString(), paramMap);
			if (offset > 0) {
				String columnName = null;
				for (Map<String, Object> map : list) {
					if (columnName == null)
						for (String s : map.keySet())
							if (s.equalsIgnoreCase("rownum_")) {
								columnName = s;
								break;
							}
					map.remove(columnName);
				}
			}
			return list;

		} else if (databaseProduct == DatabaseProduct.DB2) {
			StringBuilder sb = new StringBuilder(
					sql.length() + offset > 0 ? 200 : 20);
			if (offset > 0) {
				sb.append(
						"select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ")
						.append(sql)
						.append(" fetch first ")
						.append(limit + offset)
						.append(" rows only ) as inner2_ ) as inner1_ where rownumber_ > ")
						.append(offset).append(" order by rownumber_");
			} else {
				sb.append(sql);
				sb.append(" fetch first ");
				sb.append(limit);
				sb.append(" rows only");
			}
			List<Map<String, Object>> list = namedParameterJdbcTemplate
					.queryForList(sb.toString(), paramMap);
			if (offset > 0) {
				String columnName = null;
				for (Map<String, Object> map : list) {
					if (columnName == null)
						for (String s : map.keySet())
							if (s.equalsIgnoreCase("rownumber_")) {
								columnName = s;
								break;
							}
					map.remove(columnName);
				}
			}
			return list;
		} else if (databaseProduct == DatabaseProduct.DERBY) {
			StringBuilder sb = new StringBuilder(sql.length() + 50);
			sb.append(sql);
			if (offset == 0)
				sb.append(" fetch first ");
			else
				sb.append(" offset ").append(offset)
						.append(" rows fetch next ");
			sb.append(limit).append(" rows only");
			return namedParameterJdbcTemplate.queryForList(sb.toString(),
					paramMap);
		}

		if (databaseProduct == DatabaseProduct.SQLSERVER
				|| databaseProduct == DatabaseProduct.SYBASE) {
			int selectIndex = sql.toLowerCase().indexOf("select");
			int selectDistinctIndex = sql.toLowerCase().indexOf(
					"select distinct");
			int position = selectIndex
					+ (selectDistinctIndex == selectIndex ? 15 : 6);
			sql = new StringBuilder(sql.length() + 8)
					.append(sql)
					.insert(position,
							" top " + (offset > 0 ? offset + limit : limit))
					.toString();
			if (offset <= 0)
				return namedParameterJdbcTemplate.queryForList(sql, paramMap);
		}
		if (databaseProduct == DatabaseProduct.INFORMIX) {
			int selectIndex = sql.toLowerCase().indexOf("select");
			int selectDistinctIndex = sql.toLowerCase().indexOf(
					"select distinct");
			int position = selectIndex
					+ (selectDistinctIndex == selectIndex ? 15 : 6);
			sql = new StringBuilder(sql.length() + 8)
					.append(sql)
					.insert(position,
							" first " + (offset > 0 ? offset + limit : limit))
					.toString();
			if (offset <= 0)
				return namedParameterJdbcTemplate.queryForList(sql, paramMap);
		}

		final ColumnMapRowMapper crm = new ColumnMapRowMapper();
		return namedParameterJdbcTemplate.execute(sql, paramMap,
				new PreparedStatementCallback<List<Map<String, Object>>>() {
					@Override
					public List<Map<String, Object>> doInPreparedStatement(
							PreparedStatement preparedStatement)
							throws SQLException, DataAccessException {
						preparedStatement.setMaxRows(offset + limit);
						ResultSet rs = preparedStatement.executeQuery();
						List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(
								limit);
						int i = 0;
						while (rs.next()) {
							if (i >= offset)
								result.add(crm.mapRow(rs, i));
							i++;
						}
						rs.close();
						return result;
					}
				});
	}

	public ResultPage<Map<String, Object>> query(
			ResultPage<Map<String, Object>> resultPage) {
		QueryCriteria criteria = resultPage.getCriteria();
		String sql = criteria.getQuery();
		Map<String, ?> paramMap = criteria.getParameters();
		sql = refineSql(sql);
		validateAndConvertTypes(sql, paramMap);
		boolean hasLimit = hasLimit(sql);
		resultPage.setPaginating(!hasLimit);
		resultPage.setTotalResults(count(sql, paramMap));
		if (resultPage.getTotalResults() > ResultPage.DEFAULT_MAX_PAGESIZE
				&& (hasLimit || !(databaseProduct == DatabaseProduct.MYSQL
						|| databaseProduct == DatabaseProduct.POSTGRESQL
						|| databaseProduct == DatabaseProduct.H2
						|| databaseProduct == DatabaseProduct.HSQL
						|| databaseProduct == DatabaseProduct.ORACLE
						|| databaseProduct == DatabaseProduct.DB2 || databaseProduct == DatabaseProduct.DERBY)))
			throw new ErrorMessage("query.result.number.exceed",
					new Object[] { ResultPage.DEFAULT_MAX_PAGESIZE });
		resultPage.setResult(query(sql, paramMap, resultPage.getPageSize(),
				(resultPage.getPageNo() - 1) * resultPage.getPageSize()));
		return resultPage;
	}

	public Set<String> extractParameters(String sql) {
		Set<String> names = new LinkedHashSet<String>();
		Matcher m = PARAMETER_PATTERN.matcher(sql);
		while (m.find())
			names.add(m.group().substring(1));
		return names;
	}

	private Set<String> extractTables(String sql) {
		Set<String> names = new LinkedHashSet<String>();
		Matcher m = TABLE_PATTERN.matcher(sql);
		while (m.find()) {
			String arr[] = m.group(1).split(",");
			for (String s : arr) {
				names.add(s.trim().split("\\s+")[0]);
			}
		}
		return names;
	}

	private static String refineSql(String sql) {
		sql = sql.trim();
		if (sql.endsWith(";"))
			sql = sql.substring(0, sql.length() - 1);
		return sql;
	}

	private static String trimOrderby(String sql) {
		Matcher m = ORDERBY_PATTERN.matcher(sql);
		return m.replaceAll("");
	}

	private boolean hasLimit(String sql) {
		if (databaseProduct == DatabaseProduct.MYSQL
				|| databaseProduct == DatabaseProduct.POSTGRESQL
				|| databaseProduct == DatabaseProduct.H2
				|| databaseProduct == DatabaseProduct.HSQL) {
			return LIMIT_PATTERN.matcher(sql).find();
		} else if (databaseProduct == DatabaseProduct.DB2
				|| databaseProduct == DatabaseProduct.INFORMIX) {
			return FIRST_PATTERN.matcher(sql).find();
		} else if (databaseProduct == DatabaseProduct.DERBY) {
			return FIRST_PATTERN.matcher(sql).find()
					|| NEXT_PATTERN.matcher(sql).find();
		} else if (databaseProduct == DatabaseProduct.SQLSERVER
				|| databaseProduct == DatabaseProduct.SYBASE) {
			return TOP_PATTERN.matcher(sql).find();
		} else if (databaseProduct == DatabaseProduct.ORACLE) {
			return ROWNUM_PATTERN.matcher(sql).find();
		}
		return false;
	}

	private Pattern TABLE_PATTERN = Pattern.compile(
			"from\\s+([\\w\\.\"'`,\\s]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern PARAMETER_PATTERN = Pattern.compile(
			"(:[a-z]\\w*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ORDERBY_PATTERN = Pattern.compile(
			"\\s+order\\s+by\\s+.+$", Pattern.CASE_INSENSITIVE);

	private static final Pattern LIMIT_PATTERN = Pattern.compile(
			"\\s+limit\\s+\\d+", Pattern.CASE_INSENSITIVE);

	private static final Pattern TOP_PATTERN = Pattern.compile(
			"\\s+top\\s+\\d+\\s+", Pattern.CASE_INSENSITIVE);

	private static final Pattern FIRST_PATTERN = Pattern.compile(
			"\\s+first\\s+\\d+\\s+", Pattern.CASE_INSENSITIVE);
	private static final Pattern NEXT_PATTERN = Pattern.compile(
			"\\s+next\\s+\\d+\\s+", Pattern.CASE_INSENSITIVE);
	private static final Pattern ROWNUM_PATTERN = Pattern.compile("\\s+rownum",
			Pattern.CASE_INSENSITIVE);

}