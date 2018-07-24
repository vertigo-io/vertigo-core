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

	opens io.vertigo.database.impl.sql.i18n;
	//internationalisation a besoin d'être dans un package ouvert (ca ne pose pas de problème s'il n'y a que ca dedans...)
	//il faut juste trouver la convention de nommage à respecter et savoir où est placé ce package

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
