package org.ironrhino.core.sequence;

import java.sql.SQLException;
import java.sql.Statement;

public class OracleCyclicSequence extends AbstractSequenceCyclicSequence {

	protected String getQuerySequenceStatement() {
		return new StringBuilder("SELECT ").append(getActualSequenceName())
				.append(".NEXTVAL FROM DUAL").toString();
	}

	protected void restartSequence(Statement stmt) throws SQLException {
		stmt.execute("DROP SEQUENCE " + getActualSequenceName());
		stmt.execute(getCreateSequenceStatement());
	}

	protected String getCurrentTimestampFunction() {
		return "SYSDATE";
	}

}
