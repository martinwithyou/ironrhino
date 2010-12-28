package org.ironrhino.core.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.util.AppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInfoListener implements ServletContextListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void contextDestroyed(ServletContextEvent event) {
		System.clearProperty(AppInfo.getAppName() + ".home");
		System.clearProperty(AppInfo.getAppName() + ".context");
	}

	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();
		String name = ctx.getInitParameter(AppInfo.KEY_APP_NAME);
		if (name == null)
			name = ctx.getServletContextName();
		if (StringUtils.isNotBlank(name))
			AppInfo.setAppName(name);
		String version = ctx.getInitParameter(AppInfo.KEY_APP_VERSION);
		if (StringUtils.isNotBlank(version))
			AppInfo.setAppVersion(version);
		String home = ctx.getInitParameter(AppInfo.KEY_APP_HOME);
		if (StringUtils.isNotBlank(home)) {
			AppInfo.setAppHome(home);
		}
		System
				.setProperty(AppInfo.getAppName() + ".home", AppInfo
						.getAppHome());
		System.setProperty(AppInfo.getAppName() + ".context", ctx
				.getRealPath("/"));
		logger
				.info(
						"app.name={},app.version={},app.stage={},app.home={},hostname={},hostaddress={}",
						new String[] { AppInfo.getAppName(),
								AppInfo.getAppVersion(),
								AppInfo.getStage().toString(),
								AppInfo.getAppHome(), AppInfo.getHostName(),
								AppInfo.getHostAddress() });
	}

}
