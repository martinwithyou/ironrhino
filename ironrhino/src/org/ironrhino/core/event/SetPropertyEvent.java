package org.ironrhino.core.event;

import org.springframework.context.ApplicationEvent;

public class SetPropertyEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6493345801143748086L;

	private String expression;

	public SetPropertyEvent(String expression) {
		super(expression);
		this.expression = expression;
	}

	public String getExpression() {
		return this.expression;
	}

	public Object getSource() {
		return getExpression();
	}

	public String toString() {
		return getClass().getName() + "[source=" + getSource() + ",timestamp="
				+ getTimestamp() + "]";
	}

}
