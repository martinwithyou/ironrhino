package org.ironrhino.core.sequence;

import javax.sql.DataSource;

public abstract class AbstractDatabaseCyclicSequence extends
		AbstractCyclicSequence {

	private DataSource dataSource;

	private String columnName;

	private int cacheSize = 1;

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	protected void checkDatabaseProductName(String databaseProductName) {
		String impl = getClass().getSimpleName();
		impl = impl.substring(0, impl.indexOf("CyclicSequence"));
		if (!databaseProductName.toLowerCase().contains(impl.toLowerCase()))
			throw new RuntimeException(getClass()
					+ " is not compatibility with " + databaseProductName);
	}

	protected String getActualSequenceName() {
		return new StringBuilder(getSequenceName()).append("_")
				.append(getColumnName()).append("_SEQ").toString();
	}

}
