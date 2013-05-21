package org.ironrhino.core.schedule;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.coordination.Membership;
import org.ironrhino.core.metadata.Scope;
import org.ironrhino.core.spring.ApplicationContextConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

@Singleton
@Named
public class TimerExecutor {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private ApplicationContext ctx;

	@Inject
	private ApplicationContextConsole applicationContextConsole;

	@Inject
	private Membership membership;

	@Autowired(required = false)
	private ExecutorService executorService;

	private Map<Period, Map<String, Scope>> map = new HashMap<Period, Map<String, Scope>>();

	@PostConstruct
	public void init() {

		String[] beanNames = ctx.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			if (StringUtils.isAlphanumeric(beanName)
					&& ctx.isSingleton(beanName)) {
				Object bean = ctx.getBean(beanName);
				Method[] methods = bean.getClass().getMethods();
				for (Method m : methods) {
					int modifiers = m.getModifiers();
					if (Modifier.isStatic(modifiers)
							|| Modifier.isAbstract(modifiers)
							|| !Modifier.isPublic(modifiers))
						continue;
					if (!m.getReturnType().equals(Void.TYPE))
						continue;
					if (m.getParameterTypes().length != 0)
						continue;
					Timer timer = m.getAnnotation(Timer.class);
					if (timer == null)
						continue;
					String expression = new StringBuilder(beanName).append(".")
							.append(m.getName()).append("()").toString();
					Map<String, Scope> temp = map.get(timer.period());
					if (temp == null) {
						temp = new HashMap<String, Scope>();
						map.put(timer.period(), temp);
					}
					temp.put(expression, timer.scope());
				}
			}
		}

		for (Period p : map.keySet()) {
			membership.join(p.getFullname());
		}
	}

	public void execute(final String expression, final Scope scope) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					applicationContextConsole.execute(expression, scope);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}

			}
		};
		if (executorService != null)
			executorService.execute(task);
		else
			task.run();
	}

	@Scheduled(cron = "0 0 0 * * ?")
	public void executeOnDayStart() {
		if (!membership.isLeader(Period.DAY_START.getFullname()))
			return;
		Map<String, Scope> temp = map.get(Period.DAY_START);
		if (temp == null || temp.size() == 0)
			return;
		for (Map.Entry<String, Scope> entry : temp.entrySet()) {
			try {
				execute(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Scheduled(cron = "0 0 0 * * 1")
	public void executeOnWeekStart() {
		if (!membership.isLeader(Period.WEEK_START.getFullname()))
			return;
		Map<String, Scope> temp = map.get(Period.DAY_START);
		if (temp == null || temp.size() == 0)
			return;
		for (Map.Entry<String, Scope> entry : temp.entrySet()) {
			try {
				execute(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Scheduled(cron = "0 0 0 1 * ?")
	public void executeOnMonthStart() {
		if (!membership.isLeader(Period.MONTH_START.getFullname()))
			return;
		Map<String, Scope> temp = map.get(Period.DAY_START);
		if (temp == null || temp.size() == 0)
			return;
		for (Map.Entry<String, Scope> entry : temp.entrySet()) {
			try {
				execute(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Scheduled(cron = "0 0 0 1 1 ?")
	public void executeOnYearStart() {
		if (!membership.isLeader(Period.YEAR_START.getFullname()))
			return;
		Map<String, Scope> temp = map.get(Period.DAY_START);
		if (temp == null || temp.size() == 0)
			return;
		for (Map.Entry<String, Scope> entry : temp.entrySet()) {
			try {
				execute(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

}