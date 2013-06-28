package org.ironrhino.core.sequence;

public class H2CyclicSequence extends AbstractSequenceCyclicSequence {

	protected String getQuerySequenceStatement() {
		return new StringBuilder("CALL NEXT VALUE FOR ").append(
				getActualSequenceName()).toString();
	}

}
