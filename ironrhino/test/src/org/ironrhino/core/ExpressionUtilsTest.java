package org.ironrhino.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.ironrhino.common.util.ExpressionUtils;
import org.junit.Test;

public class ExpressionUtilsTest {

	static class User {
		private String name = "zhouyanming";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Test
	public void testEval() throws ScriptException {
		String ex = "'hello,'+user.name";
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("user", new User());
		assertEquals("hello,zhouyanming", ExpressionUtils.eval(ex, context));
	}

	@Test
	public void testRender() throws ScriptException {
		String ex = "hello,${user.name}";
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("user", new User());
		assertEquals("hello,zhouyanming", ExpressionUtils.render(ex, context));
	}

}
