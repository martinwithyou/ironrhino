package org.ironrhino.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.NativeObject;

public class ExpressionUtils {

	private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

	private static ScriptEngine sharedScriptEngine;

	static {
		ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");
		// maybe threadsafe,but thread1 can see thread2's variable
		/**
		 * A reserved key, THREADING, whose value describes the behavior of the
		 * engine with respect to concurrent execution of scripts and
		 * maintenance of state is also defined. These values for the THREADING
		 * key are:
		 * 
		 * null - The engine implementation is not thread safe, and cannot be
		 * used to execute scripts concurrently on multiple threads.
		 * 
		 * "MULTITHREADED" - The engine implementation is internally thread-safe
		 * and scripts may execute concurrently although effects of script
		 * execution on one thread may be visible to scripts on other threads.
		 * 
		 * "THREAD-ISOLATED" - The implementation satisfies the requirements of
		 * "MULTITHREADED", and also, the engine maintains independent values
		 * for symbols in scripts executing on different threads.
		 * 
		 * "STATELESS" - The implementation satisfies the requirements of
		 * "THREAD-ISOLATED". In addition, script executions do not alter the
		 * mappings in the Bindings which is the engine scope of the
		 * ScriptEngine. In particular, the keys in the Bindings and their
		 * associated values are the same before and after the execution of the
		 * script.
		 */
		if (engine.getFactory().getParameter("THREADING") != null)
			sharedScriptEngine = engine;
	}

	private static ScriptEngine getScriptEngine(boolean stateless) {
		if (!stateless && sharedScriptEngine != null)
			return sharedScriptEngine;
		return scriptEngineManager.getEngineByName("JavaScript");
	}

	public static Object eval(String expression, Map<String, Object> context)
			throws ScriptException {
		return eval(expression, context, false);
	}

	public static Object eval(String expression, Map<String, Object> context,
			boolean stateless) throws ScriptException {
		ScriptEngine engine = getScriptEngine(stateless);
		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		if (context != null)
			for (Map.Entry<String, Object> entry : context.entrySet())
				bindings.put(entry.getKey(), entry.getValue());
		Object obj = engine.eval(expression);
		bindings.clear();
		if (obj instanceof NativeArray) {
			NativeArray array = (NativeArray) obj;
			long length = array.getLength();
			List<Object> list = new ArrayList<Object>((int) length);
			for (int i = 0; i < length; i++)
				list.add(array.get(i, null));
			return list;
		} else if (obj instanceof NativeObject) {
			NativeObject object = (NativeObject) obj;
			Map<String, Object> map = new HashMap<String, Object>(object
					.getIds().length);
			for (Object id : object.getIds())
				map.put(id.toString(), object.get(id.toString(), null));
			return map;
		} else if (obj instanceof Double) {
			Double d = (Double) obj;
			if (d.doubleValue() == d.intValue())
				return Integer.valueOf(d.intValue());
			else if (d.doubleValue() == d.longValue())
				return Long.valueOf(d.longValue());
		}
		return obj;
	}

	public static String render(String template, Map<String, Object> context)
			throws ScriptException {
		return render(template, context, false);
	}

	public static String render(String template, Map<String, Object> context,
			boolean stateless) throws ScriptException {
		int begin = 0, start = template.indexOf("${"), end = template.indexOf(
				"}", start);
		StringBuilder sb = new StringBuilder();
		while (end > 0) {
			sb.append(template.substring(begin, start));
			String ex = template.substring(start + 2, end);
			sb.append(eval(ex, context));
			begin = end + 1;
			start = template.indexOf("${", begin);
			if (start < 0)
				break;
			end = template.indexOf("}", begin);
		}
		if (end < template.length() - 1)
			sb.append(template.substring(begin));
		return sb.toString();
	}

}
