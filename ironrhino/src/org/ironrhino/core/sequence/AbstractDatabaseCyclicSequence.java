package org.ironrhino.core.sequence;

import javax.sql.DataSource;

public abstract class AbstractDatabaseCyclicSequence extends
		AbstractCyclicSequence {

	private DataSource dataSource;

	private String tableName;

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

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	protected void checkDatabaseProductName(String databaseProductName) {
		String impl = getClass().getSimpleName();
		impl = impl.substring(0, impl.indexOf("CyclicSequence"));
		if (!databaseProductName.toLowerCase().contains(impl.toLowerCase()))
			throw new RuntimeException(getClass()
					+ " is not compatibility with " + databaseProductName);
	}

	protected String getActualSequenceName() {
		return new StringBuilder(getTableName()).append("_")
				.append(getSequenceName()).append("_SEQ").toString();
	}

}
