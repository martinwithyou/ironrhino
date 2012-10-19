package org.ironrhino.core.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.util.AppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInfoListener implements ServletContextListener {

	private Logger logger;

	public void contextDestroyed(ServletContextEvent event) {
		System.clearProperty(AppInfo.getAppName() + ".home");
		System.clearProperty(AppInfo.getAppName() + ".context");
		logger.info(
				"app.name={},app.version={},app.instanceid={},app.stage={},app.home={},hostname={},hostaddress={} is shutdown",
				new String[] { AppInfo.getAppName(), AppInfo.getAppVersion(),
						AppInfo.getInstanceId(), AppInfo.getStage().toString(),
						AppInfo.getAppHome(), AppInfo.getHostName(),
						AppInfo.getHostAddress() });
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
		System.setProperty(AppInfo.getAppName() + ".home", AppInfo.getAppHome());
		String context = ctx.getRealPath("/");
		if (context == null)
			context = "";
		System.setProperty(AppInfo.getAppName() + ".context", context);
		logger = LoggerFactory.getLogger(getClass());
		logger.info(
				"app.name={},app.version={},app.instanceid={},app.stage={},app.home={},hostname={},hostaddress={}",
				new String[] { AppInfo.getAppName(), AppInfo.getAppVersion(),
						AppInfo.getInstanceId(), AppInfo.getStage().toString(),
						AppInfo.getAppHome(), AppInfo.getHostName(),
						AppInfo.getHostAddress() });
	}

}
