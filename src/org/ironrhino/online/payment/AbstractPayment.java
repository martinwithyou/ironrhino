package org.ironrhino.online.payment;

public abstract class AbstractPayment implements Payment {

	protected String name;

	protected String code;

	protected boolean disabled;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getHiddenFormField(String name, Object value) {
		return "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value
				+ "\"/>\r\n";
	}
}
