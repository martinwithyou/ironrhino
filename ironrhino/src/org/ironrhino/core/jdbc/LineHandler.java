package org.ironrhino.core.jdbc;

public interface LineHandler {

	public boolean isWithHeader();

	public String getSeperator();

	public void handleLine(int index, String line);

}
