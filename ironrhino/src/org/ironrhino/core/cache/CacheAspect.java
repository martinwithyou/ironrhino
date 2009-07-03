package org.ironrhino.core.cache;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.annotation.CheckCache;
import org.ironrhino.core.annotation.FlushCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

/**
 * cache some data
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.annotation.CheckCache
 * @see org.ironrhino.core.annotation.FlushCache
 */
@Aspect
public class CacheAspect implements Ordered {

	private Log log = LogFactory.getLog(CacheAspect.class);

	private int order;

	@Autowired
	private ApplicationContext applicationContext;

	private ScriptEngine scriptEngine;

	private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

	@PostConstruct
	public void init() {
		ScriptEngine engine = getScriptEngine();
		// threadsafe
		if (engine.getFactory().getParameter("THREADING") != null)
			scriptEngine = engine;
	}

	public ScriptEngine getScriptEngine() {
		if (scriptEngine != null)
			return scriptEngine;
		return scriptEngineManager.getEngineByName("JavaScript");
	}

	@Around("@annotation(checkCache)")
	public Object get(ProceedingJoinPoint call, CheckCache checkCache)
			throws Throwable {
		String key = checkKey(call, checkCache);
		if (key == null)
			return call.proceed();
		Cache cache = getCache(checkCache.namespace());
		if (cache != null) {
			if (CacheContext.forceFlush()) {
				cache.remove(key);
			} else {
				Element element = cache.get(key);
				if (element != null) {
					return element.getValue();
				}
			}
		}
		Object result = call.proceed();
		if (cache != null) {
			Element element = new Element(key, result);
			cache.put(element);
		}
		return result;
	}

	@AfterReturning("@annotation(flushCache)")
	public void remove(JoinPoint jp, FlushCache flushCache) {
		String[] keys = flushKeys(jp, flushCache);
		if (keys == null)
			return;
		Cache cache = getCache(flushCache.namespace());
		if (cache != null)
			for (String key : keys) {
				cache.remove(key.trim());
			}
	}

	private String checkKey(JoinPoint jp, CheckCache cache) {
		try {
			return eval(cache.value(), jp.getArgs()).trim();
		} catch (ScriptException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private String[] flushKeys(JoinPoint jp, FlushCache cache) {
		String keys;
		try {
			keys = eval(cache.value(), jp.getArgs());
			return keys.split(",");
		} catch (ScriptException e) {
			log.error(e.getMessage(), e);
			return null;
		}

	}

	private String eval(String template, Object[] args) throws ScriptException {
		if (template.indexOf('$') < 0)
			return template;
		ScriptEngine engine = getScriptEngine();
		engine.put("args", args);
		int begin = 0, start = template.indexOf("${"), end = template
				.indexOf("}");
		// TODO opt
		StringBuilder sb = new StringBuilder();
		while (end > 0) {
			sb.append(template.substring(begin, start));
			String ex = template.substring(start + 2, end);
			sb.append(engine.eval(ex));
			begin = end + 1;
			start = template.indexOf("${", begin);
			end = template.indexOf("}", begin);
		}
		sb.append(template.substring(begin));
		return sb.toString();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Cache getCache(String namespace) {
		return (Cache) applicationContext.getBean(namespace);
	}

	public static void main(String... strings) {
		String template = "this is ${test} and ${haha}";
		int begin = 0, start = template.indexOf("${"), end = template
				.indexOf("}");
		StringBuilder sb = new StringBuilder();
		while (end > 0) {
			sb.append(template.substring(begin, start));
			String ex = template.substring(start + 2, end);
			// TODO ex
			sb.append("'" + ex + "'");
			begin = end + 1;
			start = template.indexOf("${", begin);
			end = template.indexOf("}", begin);
		}
		sb.append(template.substring(begin));
		System.out.println(sb.toString());
	}

}
