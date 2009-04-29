package org.ironrhino.core.ext.spring;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;

import bsh.Interpreter;

public class BeanShellApplicationContextConsole extends
		AbstractApplicationContextConsole {

	private Interpreter interpreter;

	@PostConstruct
	public void afterPropertiesSet() {
		try {
			interpreter = new Interpreter();
			interpreter.set("_this", this);
			interpreter.eval("set(path,value){_this.set(path,value);}");
			interpreter.eval("get(path){return _this.get(path);}");
			interpreter.eval("call(path){return _this.call(path,null);}");
			interpreter
					.eval("call(path,params){return _this.call(path,params);}");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Object execute(String cmd) throws Exception {
		if (StringUtils.isBlank(cmd))
			return null;
		if (!cmd.endsWith(";"))
			cmd += ";";
		return interpreter.eval(cmd);
	}
}
