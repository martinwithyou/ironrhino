package org.ironrhino.core.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.util.AppInfo;

public class AppInfoListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();
		String name = ctx.getInitParameter(AppInfo.KEY_APP_NAME);
		if (name == null)
			name = ctx.getServletContextName();
		if (StringUtils.isNotBlank(name))
			AppInfo.setName(name);
		String version = ctx.getInitParameter(AppInfo.KEY_APP_VERSION);
		if (StringUtils.isNotBlank(version))
			AppInfo.setVersion(version);
		String home = ctx.getInitParameter(AppInfo.KEY_APP_HOME);
		if (StringUtils.isNotBlank(home)) {
			AppInfo.setHome(home);
		}
		System.setProperty(AppInfo.name + ".home", AppInfo.getAppHome());
	}

}
