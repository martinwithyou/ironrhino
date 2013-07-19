package org.ironrhino.core.jdbc;

public enum DatabaseProduct {

	MYSQL, POSTGRESQL, ORACLE, DB2, INFORMIX, SQLSERVER, SYBASE, H2, HSQL, DERBY;

	public static DatabaseProduct parse(String databaseProductName) {
		if (databaseProductName.toLowerCase().contains("mysql"))
			return MYSQL;
		else if (databaseProductName.toLowerCase().contains("postgres"))
			return POSTGRESQL;
		else if (databaseProductName.toLowerCase().contains("oracle"))
			return ORACLE;
		else if (databaseProductName.toLowerCase().startsWith("db2"))
			return DB2;
		else if (databaseProductName.toLowerCase().contains("informix"))
			return INFORMIX;
		else if (databaseProductName.toLowerCase().contains("microsoft"))
			return SQLSERVER;
		else if (databaseProductName.toLowerCase().contains("sql server")
				|| databaseProductName.equals("Adaptive Server Enterprise")
				|| databaseProductName.equals("ASE"))
			return SYBASE;
		else if (databaseProductName.toLowerCase().equals("h2"))
			return H2;
		else if (databaseProductName.toLowerCase().contains("hsql"))
			return HSQL;
		else if (databaseProductName.toLowerCase().contains("derby"))
			return DERBY;
		return null;
	}

}
