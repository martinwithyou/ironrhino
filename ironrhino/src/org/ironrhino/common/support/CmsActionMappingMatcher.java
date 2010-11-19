package org.ironrhino.common.support;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.ironrhino.common.Constants;
import org.ironrhino.common.model.Setting;
import org.ironrhino.core.event.EntityOperationEvent;
import org.ironrhino.core.struts.ActionMappingMatcher;
import org.ironrhino.core.struts.DefaultActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@Named
@Singleton
public class CmsActionMappingMatcher implements ActionMappingMatcher,
		ApplicationListener {

	public static final String DEFAULT_PAGE_PATH_PREFIX = "/p/";

	@Value("${cms.pagePathPrefix:" + DEFAULT_PAGE_PATH_PREFIX + "}")
	private String pagePathPrefix = DEFAULT_PAGE_PATH_PREFIX;

	@Value("${" + Constants.SETTING_KEY_CMS_SERIESES + ":}")
	private String serieses = "";

	@Value("${" + Constants.SETTING_KEY_CMS_COLUMNS + ":}")
	private String columns = "";

	@Value("${" + Constants.SETTING_KEY_CMS_ISSUES + ":}")
	private String issues = "";

	private List<String> seriesesList;

	private List<String> columnsList;

	private List<String> issuesList;

	@Autowired(required = false)
	private SettingControl settingControl;

	@PostConstruct
	public void afterPropertiesSet() {
		buildSerieses();
		buildColumns();
		buildIssues();
	}

	public ActionMapping tryMatch(HttpServletRequest request,
			DefaultActionMapper actionMapper) {
		String uri = actionMapper.getUri(request);
		String encoding = actionMapper.getEncoding();
		if (uri.startsWith(pagePathPrefix)) {
			String pagePath = uri.substring(pagePathPrefix.length() - 1);
			ActionMapping mapping = new ActionMapping();
			mapping.setNamespace("/");
			mapping.setName("displayPage");
			Map<String, Object> params = new HashMap<String, Object>(3);
			params.put(DefaultActionMapper.ID, pagePath);
			mapping.setParams(params);
			return mapping;
		}
		for (String name : seriesesList) {
			String pageurl = new StringBuilder("/").append(name).append(
					DEFAULT_PAGE_PATH_PREFIX).toString();
			if (uri.equals("/" + name) || uri.startsWith(pageurl)) {
				ActionMapping mapping = new ActionMapping();
				mapping.setNamespace("/");
				mapping.setName("seriesPage");
				Map<String, Object> params = new HashMap<String, Object>(3);
				params.put("name", name);
				if (uri.startsWith(pageurl)) {
					params.put(DefaultActionMapper.ID, uri.substring(pageurl
							.length()));
				}
				mapping.setParams(params);
				return mapping;
			}
		}
		for (String name : columnsList) {
			String listurl = new StringBuilder("/").append(name).append(
					"/list/").toString();
			String pageurl = new StringBuilder("/").append(name).append(
					DEFAULT_PAGE_PATH_PREFIX).toString();
			if (uri.equals("/" + name) || uri.startsWith(listurl)
					|| uri.startsWith(pageurl)) {
				ActionMapping mapping = new ActionMapping();
				mapping.setNamespace("/");
				mapping.setName("columnPage");
				Map<String, Object> params = new HashMap<String, Object>(3);
				params.put("name", name);
				if (uri.startsWith(listurl)) {
					mapping.setMethod("list");
					try {
						params.put(DefaultActionMapper.ID, URLDecoder.decode(
								uri.substring(listurl.length()), encoding));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else if (uri.startsWith(pageurl)) {
					mapping.setMethod("p");
					try {
						params.put(DefaultActionMapper.ID, URLDecoder.decode(
								uri.substring(pageurl.length()), encoding));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				mapping.setParams(params);
				return mapping;
			}
		}
		for (String name : issuesList) {
			String listurl = new StringBuilder("/").append(name).append(
					"/list/").toString();
			String pageurl = new StringBuilder("/").append(name).append(
					DEFAULT_PAGE_PATH_PREFIX).toString();
			if (uri.equals("/" + name) || uri.startsWith(listurl)
					|| uri.startsWith(pageurl)) {
				ActionMapping mapping = new ActionMapping();
				mapping.setNamespace("/");
				mapping.setName("issuePage");
				Map<String, Object> params = new HashMap<String, Object>(3);
				params.put("name", name);
				if (uri.startsWith(listurl)) {
					mapping.setMethod("list");
					try {
						params.put(DefaultActionMapper.ID, URLDecoder.decode(
								uri.substring(listurl.length()), encoding));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else if (uri.startsWith(pageurl)) {
					mapping.setMethod("p");
					try {
						params.put(DefaultActionMapper.ID, URLDecoder.decode(
								uri.substring(pageurl.length()), encoding));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				mapping.setParams(params);
				return mapping;
			}
		}
		return null;
	}

	private void buildSerieses() {
		List<String> list = new ArrayList<String>();
		if (StringUtils.isNotBlank(serieses))
			for (String s : serieses.split(","))
				list.add(s);
		if (settingControl != null)
			for (String s : settingControl
					.getStringArray(Constants.SETTING_KEY_CMS_SERIESES))
				list.add(s);
		seriesesList = list;
	}

	private void buildColumns() {
		List<String> list = new ArrayList<String>();
		if (StringUtils.isNotBlank(columns))
			for (String s : columns.split(","))
				list.add(s);
		if (settingControl != null)
			for (String s : settingControl
					.getStringArray(Constants.SETTING_KEY_CMS_COLUMNS))
				list.add(s);
		columnsList = list;
	}

	private void buildIssues() {
		List<String> list = new ArrayList<String>();
		if (StringUtils.isNotBlank(issues))
			for (String s : issues.split(","))
				list.add(s);
		if (settingControl != null)
			for (String s : settingControl
					.getStringArray(Constants.SETTING_KEY_CMS_ISSUES))
				list.add(s);
		issuesList = list;
	}

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof EntityOperationEvent) {
			EntityOperationEvent ev = (EntityOperationEvent) event;
			if (ev.getEntity() instanceof Setting) {
				Setting settingInEvent = (Setting) ev.getEntity();
				if (settingInEvent.getKey().equals(
						Constants.SETTING_KEY_CMS_SERIESES)) {
					buildSerieses();
				} else if (settingInEvent.getKey().equals(
						Constants.SETTING_KEY_CMS_COLUMNS)) {
					buildColumns();
				} else if (settingInEvent.getKey().equals(
						Constants.SETTING_KEY_CMS_ISSUES)) {
					buildIssues();
				}
			}
		}
	}
}
