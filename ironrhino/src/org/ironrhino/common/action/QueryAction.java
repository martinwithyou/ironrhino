package org.ironrhino.common.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.jdbc.JdbcQueryService;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.struts.BaseAction;

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
}
