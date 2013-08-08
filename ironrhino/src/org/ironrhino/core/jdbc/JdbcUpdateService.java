package org.ironrhino.core.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

@Singleton
@Named
public class JdbcUpdateService {

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
	}

	@Transactional(readOnly = true)
	public void validate(String sql) {
		sql = SqlUtils.refineSql(sql);
		if (!sql.toLowerCase().startsWith("update ")) {

		}
		if (restricted) {
			for (String table : SqlUtils.extractTables(sql, quoteString)) {
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
		Set<String> names = SqlUtils.extractParameters(sql);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String name : names)
			paramMap.put(name, "0");
		validateAndConvertTypes(sql, paramMap);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validateAndConvertTypes(String sql, Map paramMap) {
		try {
			// TODO if not append 1=0 then append 1=0
			update(sql, paramMap);
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

	@Transactional
	public int update(String sql, Map<String, ?> paramMap) {
		return namedParameterJdbcTemplate.update(sql, paramMap);
	}

}
