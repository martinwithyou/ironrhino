package org.ironrhino.common.action;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.jdbc.JdbcQueryService;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowCallbackHandler;

@AutoConfig
public class QueryAction extends BaseAction {

	private static final long serialVersionUID = 8180265410790553918L;

	private String sql;

	private Set<String> paramNames;

	private Map<String, String> params = new HashMap<String, String>();

	private ResultPage<Map<String, Object>> resultPage;

	@Inject
	private transient JdbcQueryService jdbcQueryService;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public Set<String> getParamNames() {
		return paramNames;
	}

	public ResultPage<Map<String, Object>> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Map<String, Object>> resultPage) {
		this.resultPage = resultPage;
	}

	@Override
	public String execute() {
		if (StringUtils.isNotBlank(sql)) {
			jdbcQueryService.validate(sql);
			paramNames = jdbcQueryService.extractParameters(sql);
			if (paramNames.size() > 0) {
				for (String s : paramNames) {
					if (!params.containsKey(s)) {
						return SUCCESS;
					}
				}
			}
			if (resultPage == null) {
				resultPage = new ResultPage<>();
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(params);
			resultPage = jdbcQueryService.query(sql, map, resultPage);
		}
		return SUCCESS;
	}

	public String export() throws Exception {
		if (StringUtils.isNotBlank(sql)) {
			jdbcQueryService.validate(sql);
			paramNames = jdbcQueryService.extractParameters(sql);
			if (paramNames.size() > 0) {
				for (String s : paramNames) {
					if (!params.containsKey(s)) {
						return SUCCESS;
					}
				}
			}
			HttpServletResponse response = ServletActionContext.getResponse();
			response.setHeader("Content-type", "text/csv");
			response.setHeader("Content-disposition",
					"attachment;filename=data.csv");
			final PrintWriter writer = response.getWriter();
			Map<String, Object> map = new HashMap<String, Object>();
			map.putAll(params);
			final ColumnMapRowMapper crm = new ColumnMapRowMapper();
			final AtomicInteger ai = new AtomicInteger(0);
			jdbcQueryService.query(sql, map, new RowCallbackHandler() {

				@Override
				public void processRow(ResultSet rs) throws SQLException {
					int index = ai.getAndIncrement();
					Map<String, Object> map = crm.mapRow(rs, index);
					if (index == 0) {
						writer.println(StringUtils.join(map.keySet(), ","));
					}
					writer.println(StringUtils.join(map.values(), ","));
					if (index > 0 && index % 100 == 0)
						writer.flush();
				}
			});
			writer.close();
		}
		return NONE;
	}
}
