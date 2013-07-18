package org.ironrhino.core.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.engine.spi.RowSelection;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Singleton
@Named
public class JdbcQueryService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Value("${hibernate.dialect:org.hibernate.dialect.MySQL5Dialect}")
	private String hibernateDialect;

	private Dialect dialect;

	public void setNamedParameterJdbcTemplate(
			NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void setHibernateDialect(String hibernateDialect) {
		this.hibernateDialect = hibernateDialect;
	}

	@PostConstruct
	public void init() {
		try {
			dialect = (Dialect) Class.forName(hibernateDialect).newInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			dialect = new MySQL5Dialect();
		}
	}

	public void validate(String sql) {
		Set<String> names = extractParameters(sql);
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (String name : names)
			parameters.put(name, "0");
		try {
			query(sql, parameters, 1);
		} catch (DataAccessException e) {
			throw new ErrorMessage(e.getMessage());
		}
	}

	public long count(String sql, Map<String, Object> parameters) {
		String alias = "_tfc_";
		while (sql.contains(alias))
			alias += "0";
		StringBuilder sb = new StringBuilder("select count(*) from (")
				.append(trimOrderby(sql)).append(") ").append(alias);
		return namedParameterJdbcTemplate.queryForObject(sb.toString(),
				parameters, Long.class);
	}

	public List<Map<String, Object>> query(String sql,
			Map<String, Object> parameters) {
		return namedParameterJdbcTemplate.queryForList(sql, parameters);
	}

	public List<Map<String, Object>> query(String sql,
			Map<String, Object> parameters, final int limit) {
		return query(sql, parameters, limit, 0);
	}

	public List<Map<String, Object>> query(String sql,
			Map<String, Object> parameters, final int limit, final int offset) {
		RowSelection rowSelection = new RowSelection();
		rowSelection.setFirstRow(0);
		rowSelection.setMaxRows(limit);
		LimitHandler lm = dialect.buildLimitHandler(sql, rowSelection);
		if (lm.supportsLimitOffset()) {
			rowSelection.setFirstRow(offset);
			sql = lm.getProcessedSql();
			if (dialect instanceof MySQLDialect) {
				if (offset > 0) {
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + offset);
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + limit);
				} else {
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + limit);
				}
				return namedParameterJdbcTemplate.queryForList(sql, parameters);
			} else if (dialect instanceof PostgreSQL81Dialect) {
				if (offset > 0) {
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + limit);
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + offset);
				} else {
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + limit);
				}
				return namedParameterJdbcTemplate.queryForList(sql, parameters);
			} else if (dialect instanceof Oracle8iDialect) {
				if (offset > 0) {
					sql = sql.replaceFirst("\\s+\\?\\s*", " "
							+ (limit + offset));
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + offset);

				} else {
					sql = sql.replaceFirst("\\s+\\?\\s*", " " + limit);
				}
				return namedParameterJdbcTemplate.queryForList(sql, parameters);
			}
		}
		if (lm.supportsLimit()) {
			sql = lm.getProcessedSql();
			sql = sql.replaceFirst("\\s+\\?\\s*", " " + limit);
			List<Map<String, Object>> result = namedParameterJdbcTemplate
					.queryForList(sql, parameters);
			if (offset <= 0)
				return result;
			else if (result.size() > offset) {
				return result.subList(offset, result.size());
			} else {
				return Collections.emptyList();
			}
		} else {
			return namedParameterJdbcTemplate.execute(sql,
					new PreparedStatementCallback<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> doInPreparedStatement(
								PreparedStatement preparedStatement)
								throws SQLException, DataAccessException {
							preparedStatement.setMaxRows(offset + limit);
							ResultSet rs = preparedStatement.executeQuery();
							ColumnMapRowMapper crm = new ColumnMapRowMapper();
							List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
							int i = 0;
							while (rs.next())
								if (i >= offset)
									result.add(crm.mapRow(rs, i++));
							rs.close();
							return result;
						}
					});
		}
	}

	public ResultPage<Map<String, Object>> query(String sql,
			Map<String, Object> parameters,
			ResultPage<Map<String, Object>> resultPage) {
		resultPage.setTotalResults(count(sql, parameters));
		if (resultPage.getTotalResults() > ResultPage.DEFAULT_MAX_PAGESIZE)
			throw new ErrorMessage("number of results exceed "
					+ ResultPage.DEFAULT_MAX_PAGESIZE);
		resultPage.setResult(query(sql, parameters, resultPage.getPageSize(),
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

	private static String trimOrderby(String sql) {
		Matcher m = ORDERBY_PATTERN.matcher(sql);
		return m.replaceAll("");
	}

	private static final Pattern PARAMETER_PATTERN = Pattern
			.compile("(:[a-z]\\w*)");

	private static final Pattern ORDERBY_PATTERN = Pattern
			.compile("\\s+order\\s+by\\s+.+$");

}
