package org.ironrhino.core.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.util.AppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class AppInfoListener implements ServletContextListener {

	private Logger logger;

	public void contextInitialized(ServletContextEvent event) {
		String defaultProfiles = System
				.getenv(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME
						.replaceAll("\\.", "_").toUpperCase());
		if (StringUtils.isNotBlank(defaultProfiles))
			System.setProperty(
					AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME,
					defaultProfiles);
		ServletContext ctx = event.getServletContext();
		String name = getAppName();
		if (StringUtils.isBlank(name)) {
			name = ctx.getInitParameter(AppInfo.KEY_APP_NAME);
			if (StringUtils.isBlank(name))
				name = ctx.getServletContextName();
		}
		if (StringUtils.isNotBlank(name))
			AppInfo.setAppName(name);
		String version = ctx.getInitParameter(AppInfo.KEY_APP_VERSION);
		if (StringUtils.isNotBlank(version))
			AppInfo.setAppVersion(version);
		String home = ctx.getInitParameter(AppInfo.KEY_APP_HOME);
		if (StringUtils.isNotBlank(home)) {
			AppInfo.setAppHome(home);
		}
		System.setProperty(AppInfo.KEY_APP_HOME, AppInfo.getAppHome());
		System.setProperty(AppInfo.KEY_APP_NAME, AppInfo.getAppName());
		String context = ctx.getRealPath("/");
		if (context == null)
			context = "";
		System.setProperty("app.context", context);

		String userTimezone = System.getProperty("user.timezone");
		if (StringUtils.isBlank(userTimezone)
				|| !TimeZone.getTimeZone(userTimezone).getID()
						.equals(userTimezone)) {
			userTimezone = "Asia/Shanghai";
			TimeZone older = TimeZone.getDefault();
			TimeZone newer = TimeZone.getTimeZone(userTimezone);
			if (!newer.getID().equals(older.getID()))
				TimeZone.setDefault(newer);
		}
		logger = LoggerFactory.getLogger(getClass());
		logger.info("default timezone {}", TimeZone.getDefault().getID());
		logger.info(
				"app.name={},app.version={},app.instanceid={},app.stage={},app.home={},hostname={},hostaddress={},profiles={}",
				AppInfo.getAppName(), AppInfo.getAppVersion(),
				AppInfo.getInstanceId(), AppInfo.getStage().toString(),
				AppInfo.getAppHome(), AppInfo.getHostName(),
				AppInfo.getHostAddress(), defaultProfiles);
	}

	public void contextDestroyed(ServletContextEvent event) {
		logger.info(
				"app.name={},app.version={},app.instanceid={},app.stage={},app.home={},hostname={},hostaddress={} is shutdown",
				AppInfo.getAppName(), AppInfo.getAppVersion(),
				AppInfo.getInstanceId(), AppInfo.getStage().toString(),
				AppInfo.getAppHome(), AppInfo.getHostName(),
				AppInfo.getHostAddress());
	}

	private String getAppName() {
		Resource resource = new ClassPathResource(
				"resources/spring/applicationContext.properties");
		if (resource.exists()) {
			Properties p = new Properties();
			try (InputStream is = resource.getInputStream()) {
				p.load(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return p.getProperty("APP_NAME");
		}
		return null;
	}

}
