package org.ironrhino.core.sequence;


public class HSQLCyclicSequence extends AbstractSequenceCyclicSequence {

	protected String getCreateSequenceStatement() {
		return new StringBuilder("CREATE SEQUENCE ")
				.append(getActualSequenceName())
				.append("AS BIGINT START WITH 1").toString();
	}

	protected String getQuerySequenceStatement() {
		return new StringBuilder("CALL NEXT VALUE FOR ").append(
				getActualSequenceName()).toString();
	}

}
