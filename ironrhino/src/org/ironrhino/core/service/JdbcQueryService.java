package org.ironrhino.core.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.ironrhino.core.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

@Singleton
@Named
public class JdbcQueryService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private JdbcTemplate jdbcTemplate;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private String databaseProductName;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

	@PostConstruct
	public void init() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(
				jdbcTemplate);
		if (StringUtils.isBlank(databaseProductName)) {
			Connection con = DataSourceUtils.getConnection(jdbcTemplate
					.getDataSource());
			try {
				databaseProductName = con.getMetaData()
						.getDatabaseProductName();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			} finally {
				DataSourceUtils.releaseConnection(con,
						jdbcTemplate.getDataSource());
			}
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
		String alias = "tfc";
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
			Map<String, Object> paramMap, final int limit, final int offset) {
		if (databaseProductName.toLowerCase().contains("mysql")
				|| databaseProductName.toLowerCase().contains("postgres")
				|| databaseProductName.toLowerCase().equals("h2")) {
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
		} else if (databaseProductName.toLowerCase().contains("hsql")) {
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
		} else if (databaseProductName.toLowerCase().contains("oracle")) {
			sql = sql.trim();
			boolean isForUpdate = false;
			if (sql.toLowerCase().endsWith(" for update")) {
				sql = sql.substring(0, sql.length() - 11);
				isForUpdate = true;
			}
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
			if (isForUpdate) {
				sb.append(" for update");
			}
			return namedParameterJdbcTemplate.queryForList(sb.toString(),
					paramMap);
		} else if (databaseProductName.toLowerCase().startsWith("db2")) {
			StringBuilder sb;
			if (offset > 0) {
				sb = new StringBuilder(sql.length() + 200)
						.append("select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ")
						.append(sql)
						.append(" fetch first ")
						.append(limit)
						.append(" rows only ) as inner2_ ) as inner1_ where rownumber_ > ")
						.append(offset).append(" order by rownumber_");
			} else {
				sb = new StringBuilder(sql.length() + 20);
				sb.append(sql);
				sb.append(" fetch first ");
				sb.append(limit);
				sb.append(" rows only");
			}
			return namedParameterJdbcTemplate.queryForList(sb.toString(),
					paramMap);
		} else if (databaseProductName.toLowerCase().contains("derby")) {
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

		if (databaseProductName.toLowerCase().contains("sql server")
				|| databaseProductName.equals("Adaptive Server Enterprise")
				|| databaseProductName.equals("ASE")) {
			int selectIndex = sql.toLowerCase().indexOf("select");
			final int selectDistinctIndex = sql.toLowerCase().indexOf(
					"select distinct");
			int position = selectIndex
					+ (selectDistinctIndex == selectIndex ? 15 : 6);
			sql = new StringBuilder(sql.length() + 8).append(sql)
					.insert(position, " top " + limit).toString();
			if (offset <= 0)
				return namedParameterJdbcTemplate.queryForList(sql, paramMap);
		}
		return namedParameterJdbcTemplate.execute(sql,
				new PreparedStatementCallback<List<Map<String, Object>>>() {
					@Override
					public List<Map<String, Object>> doInPreparedStatement(
							PreparedStatement preparedStatement)
							throws SQLException, DataAccessException {
						preparedStatement.setMaxRows(offset + limit);
						ResultSet rs = preparedStatement.executeQuery();
						ColumnMapRowMapper crm = new ColumnMapRowMapper();
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

	public ResultPage<Map<String, Object>> query(String sql,
			Map<String, Object> parameters,
			ResultPage<Map<String, Object>> resultPage) {
		resultPage.setTotalResults(count(sql, parameters));
		if (resultPage.getTotalResults() > ResultPage.DEFAULT_MAX_PAGESIZE
				&& !(databaseProductName.toLowerCase().contains("mysql")
						|| databaseProductName.toLowerCase().contains(
								"postgres")
						|| databaseProductName.toLowerCase().equals("h2")
						|| databaseProductName.toLowerCase().contains("hsql")
						|| databaseProductName.toLowerCase().contains("oracle")
						|| databaseProductName.toLowerCase().startsWith("db2") || databaseProductName
						.toLowerCase().contains("derby")))
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
