package io.vertigo.dynamo.plugins.database.connection.mock;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.plugins.database.connection.AbstractConnectionProviderPlugin;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation d'un pseudo Pool.
 * 
 * @see io.vertigo.dynamo.plugins.database.connection.datasource.DataSourceConnectionProviderPlugin Utiliser une DataSource
 * @deprecated NE DOIT PAS ETRE UTILISE EN PRODUCTION.
 * @version $Id: MockConnectionProviderPlugin.java,v 1.4 2013/10/22 12:34:28 pchretien Exp $
 */
@Deprecated
public final class MockConnectionProviderPlugin extends AbstractConnectionProviderPlugin {
	/** Url Jdbc. */
	private final String jdbcUrl;

	/**
	 * Constructeur (deprecated).
	 * @param dataBaseClass Type de base de données
	 * @param jdbcDriver Classe du driver jdbc
	 * @param jdbcUrl URL de configuration jdbc
	 */
	@Inject
	public MockConnectionProviderPlugin(@Named("dataBaseClass") final String dataBaseClass, @Named("jdbcDriver") final String jdbcDriver, @Named("jdbcUrl") final String jdbcUrl) {
		super(ClassUtil.newInstance(dataBaseClass, DataBase.class));

		Assertion.checkNotNull(jdbcUrl);
		Assertion.checkNotNull(jdbcDriver);
		//----------------------------------------------------------------------
		ClassUtil.classForName(jdbcDriver); //Initialisation du driver

		this.jdbcUrl = jdbcUrl;
	}

	/** {@inheritDoc} */
	public KConnection obtainConnection() throws SQLException {
		//Dans le pseudo pool on crée systématiquement une connexion
		return new KConnection(DriverManager.getConnection(jdbcUrl), getDataBase(), true);
	}
}
