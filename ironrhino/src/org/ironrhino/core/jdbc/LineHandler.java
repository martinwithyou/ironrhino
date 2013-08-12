package org.ironrhino.core.jdbc;

public interface LineHandler {

	public boolean isWithHeader();

	public char getSeperatorChar();

	public void handleLine(int index, String line);

}
