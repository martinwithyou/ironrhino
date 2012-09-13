package org.ironrhino.core.sequence;

public class PostgreSQLCyclicSequence extends AbstractSequenceCyclicSequence {

	protected String getQuerySequenceStatement() {
		return "select nextval('" + getActualSequenceName() + "')";
	}

	protected String getCreateSequenceStatement() {
		return "CREATE  SEQUENCE " + getActualSequenceName() + " CACHE "
				+ getCacheSize();
	}

	protected String getRestartSequenceStatement() {
		return "ALTER SEQUENCE " + getActualSequenceName() + " RESTART WITH 1";
	}

	protected String getCurrentTimestampFunction() {
		return "now()::abstime::int4";
	}

}
