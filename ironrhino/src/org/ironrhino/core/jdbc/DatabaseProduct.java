package org.ironrhino.core.jdbc;

public enum DatabaseProduct {

	MYSQL, POSTGRESQL, ORACLE {
		@Override
		public String getValidationQuery() {
			return "SELECT 1 FROM DUAL";
		}
	},
	DB2 {
		@Override
		public String getValidationQuery() {
			return "VALUES 1";
		}
	},
	INFORMIX {
		@Override
		public String getValidationQuery() {
			return "SELECT FIRST 1 CURRENT FROM SYSTABLES";
		}
	},
	SQLSERVER, SYBASE, H2, HSQL {
		@Override
		public String getValidationQuery() {
			return "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
		}
	},
	DERBY {
		@Override
		public String getValidationQuery() {
			return "SELECT 1 FROM SYSIBM.SYSDUMMY1";
		}
	};

	public static DatabaseProduct parse(String nameOrUrl) {
		if (nameOrUrl.toLowerCase().startsWith("jdbc:")) {
			nameOrUrl = nameOrUrl.trim();
			if (nameOrUrl.toLowerCase().startsWith("jdbc:mysql:"))
				return MYSQL;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:postgresql:"))
				return POSTGRESQL;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:oracle:"))
				return ORACLE;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:db2:"))
				return DB2;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:informix"))
				return INFORMIX;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:sqlserver:"))
				return SQLSERVER;
			else if (nameOrUrl.toLowerCase().contains(":sybase:"))
				return SYBASE;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:h2:"))
				return H2;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:hsqldb:"))
				return HSQL;
			else if (nameOrUrl.toLowerCase().startsWith("jdbc:derby:"))
				return DERBY;
			return null;
		} else {
			if (nameOrUrl.toLowerCase().contains("mysql"))
				return MYSQL;
			else if (nameOrUrl.toLowerCase().contains("postgres"))
				return POSTGRESQL;
			else if (nameOrUrl.toLowerCase().contains("oracle"))
				return ORACLE;
			else if (nameOrUrl.toLowerCase().startsWith("db2"))
				return DB2;
			else if (nameOrUrl.toLowerCase().contains("informix"))
				return INFORMIX;
			else if (nameOrUrl.toLowerCase().contains("microsoft"))
				return SQLSERVER;
			else if (nameOrUrl.toLowerCase().contains("sql server")
					|| nameOrUrl.equals("Adaptive Server Enterprise")
					|| nameOrUrl.equals("ASE"))
				return SYBASE;
			else if (nameOrUrl.toLowerCase().equals("h2"))
				return H2;
			else if (nameOrUrl.toLowerCase().contains("hsql"))
				return HSQL;
			else if (nameOrUrl.toLowerCase().contains("derby"))
				return DERBY;
		}
		return null;
	}

	public String getValidationQuery() {
		return "SELECT 1";
	}

}
