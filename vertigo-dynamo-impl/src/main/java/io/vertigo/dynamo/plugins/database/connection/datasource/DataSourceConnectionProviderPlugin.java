package io.vertigo.dynamo.plugins.database.connection.datasource;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.plugins.database.connection.AbstractConnectionProviderPlugin;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * ConnectionProvider permettant la connexion à une datasource Java.
 *
 * @author alauthier
 * @version $Id: DataSourceConnectionProviderPlugin.java,v 1.5 2013/10/22 12:30:57 pchretien Exp $
 */
public final class DataSourceConnectionProviderPlugin extends AbstractConnectionProviderPlugin {
	/**
	 * DataSource
	 */
	private final DataSource dataSource;

	/**
	 * Constructeur.
	 * @param dataBaseName Nom du type de base de données
	 * @param dataSource URL de la dataSource JNDI
	 */
	@Inject
	public DataSourceConnectionProviderPlugin(@Named("classname") final String dataBaseName, @Named("source") final String dataSource) {
		super(createDataBase(dataBaseName));
		Assertion.checkNotNull(dataSource);
		//----------------------------------------------------------------------
		// Initialisation de la source de données
		try {
			final javax.naming.Context context = new javax.naming.InitialContext();
			this.dataSource = (DataSource) context.lookup(dataSource);
		} catch (final NamingException e) {
			throw new VRuntimeException("Impossible de récupérer la DataSource", e);
		}
	}

	/** {@inheritDoc} */
	public KConnection obtainConnection() throws SQLException {
		final java.sql.Connection connection = dataSource.getConnection();
		return new KConnection(connection, getDataBase(), true);
	}

	private static DataBase createDataBase(final String dataBaseName) {
		return ClassUtil.newInstance(dataBaseName, DataBase.class);
	}
}
