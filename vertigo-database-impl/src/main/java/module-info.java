/**
 *
 */
/**
 * @author mlaroche
 *
 */
module io.vertigo.database {
	exports io.vertigo.database;
	exports io.vertigo.database.sql;
	exports io.vertigo.database.sql.connection;
	exports io.vertigo.database.sql.mapper;
	exports io.vertigo.database.sql.statement;
	exports io.vertigo.database.sql.vendor;

	requires c3p0;
	requires hibernate.core;
	requires hibernate.jpa;
	requires io.vertigo.commons;
	requires io.vertigo.core;
	requires java.desktop;
	requires java.naming;
	requires java.sql;
	requires javax.inject;
	requires log4j.api;
}
