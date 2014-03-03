package io.vertigo.dynamo.plugins.database.connection;

import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.vendor.DataBase;
import io.vertigo.dynamo.impl.database.ConnectionProviderPlugin;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.lang.Assertion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de base des fournisseurs de connexions dynamo.
 *
 * @author pchretien
 * @version $Id: AbstractConnectionProviderPlugin.java,v 1.5 2013/11/15 14:29:55 pchretien Exp $
 */
public abstract class AbstractConnectionProviderPlugin implements ConnectionProviderPlugin, Describable {
	/**
	 * Base de données utilisée
	 */
	private final DataBase dataBase;

	/**
	 * Constructeur.
	 * @param dataBase Type de base de données
	 */
	protected AbstractConnectionProviderPlugin(final DataBase dataBase) {
		Assertion.checkNotNull(dataBase);
		Assertion.checkNotNull(dataBase.getSqlMapping());
		Assertion.checkNotNull(dataBase.getSqlExceptionHandler());
		//---------------------------------------------------------------------
		this.dataBase = dataBase;
	}

	//----------------------GESTION DU CONNECTION PROVIDER----------------------
	/** {@inheritDoc} */
	public final DataBase getDataBase() {
		return dataBase;
	}

	@Override
	public final List<ComponentInfo> getInfos() {
		final List<ComponentInfo> componentInfos = new ArrayList<>();
		try {
			// 
			final KConnection connection = obtainConnection();
			final Connection jdbcConnection = connection.getJdbcConnection();//NOPMD
			try {
				final DatabaseMetaData metaData = jdbcConnection.getMetaData();
				//---
				componentInfos.add(new ComponentInfo("database.name", metaData.getDatabaseProductName()));
				componentInfos.add(new ComponentInfo("database.version", metaData.getDatabaseProductVersion()));

				componentInfos.add(new ComponentInfo("database.driver.name", metaData.getDriverName()));
				componentInfos.add(new ComponentInfo("database.driver.name", metaData.getDriverVersion()));
				componentInfos.add(new ComponentInfo("database.driver.url", metaData.getURL()));
			} finally {
				connection.rollback();
				connection.release();
			}
		} catch (final Exception ex) {
			//
		}
		return componentInfos;
	}
}
