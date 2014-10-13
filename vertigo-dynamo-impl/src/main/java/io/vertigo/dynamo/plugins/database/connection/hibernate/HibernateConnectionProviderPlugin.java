/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.database.connection.hibernate;

import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.transaction.KTransactionManager;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

/**
 * ConnectionProvider permettant la connexion à une datasource Java.
 *
 * @author pchretien, npiedeloup
 */
public final class HibernateConnectionProviderPlugin extends JpaConnectionProviderPlugin {

	/**
	 * Constructeur.
	 * @param dataBaseName Nom du type de base de données
	 * @param persistenceUnit Nom de la persistenceUnit à utiliser (dans le persistence.xml)
	 */
	@Inject
	public HibernateConnectionProviderPlugin(@Named("persistenceUnit") final String persistenceUnit, @Named("dataBaseName") final String dataBaseName, final KTransactionManager transactionManager) {
		super(persistenceUnit, dataBaseName, transactionManager);
	}

	/** {@inheritDoc} */
	@Override
	public SqlConnection obtainWrappedConnection(final EntityManager em) {
		//preconisation StackOverFlow to get current jpa connection
		final Session session = em.unwrap(Session.class);
		return session.doReturningWork(new ReturningWork<SqlConnection>() {
			public SqlConnection execute(final Connection connection) throws SQLException {
				return new SqlConnection(connection, getDataBase(), false);
			}
		});
	}
}
