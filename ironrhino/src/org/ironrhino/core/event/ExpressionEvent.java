package org.ironrhino.core.event;

public class ExpressionEvent extends BaseEvent {

	private static final long serialVersionUID = 6493345801143748086L;

	private String expression;

	public ExpressionEvent() {
		super();
	}

	public ExpressionEvent(String expression) {
		super(expression);
		this.expression = expression;
	}

	public String getExpression() {
		return this.expression;
	}

	@Override
	public Object getSource() {
		return getExpression();
	}

	@Override
	public String toString() {
		return getClass().getName() + "[source=" + getSource() + ",timestamp="
				+ getTimestamp() + "]";
	}

}
