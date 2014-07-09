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
package io.vertigo.dynamo.impl.database;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.dynamo.database.DataBaseManager;
import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KCallableStatement;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.statement.KCallableStatementImpl;
import io.vertigo.dynamo.impl.database.statement.KPreparedStatementImpl;
import io.vertigo.dynamo.impl.database.statement.StatementHandler;
import io.vertigo.dynamo.impl.database.statementhandler.StatementHandlerImpl;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class DataBaseManagerImpl implements DataBaseManager, Activeable {
	private final DataBaseListenerImpl dataBaseListener;
	private final StatementHandler statementHandler;
	private final ConnectionProviderPlugin connectionProviderPlugin;
	private final LocaleManager localeManager;

	/**
	 * Constructeur.
	 * @param localeManager Manager des messages localisés
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public DataBaseManagerImpl(final LocaleManager localeManager, final AnalyticsManager analyticsManager, final ConnectionProviderPlugin connectionProviderPlugin) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(analyticsManager);
		Assertion.checkNotNull(connectionProviderPlugin);
		//---------------------------------------------------------------------
		dataBaseListener = new DataBaseListenerImpl(analyticsManager);
		statementHandler = new StatementHandlerImpl();
		this.localeManager = localeManager;
		this.connectionProviderPlugin = connectionProviderPlugin;
	}

	/** {@inheritDoc} */
	public void start() {
		localeManager.add("io.vertigo.dynamo.impl.database.DataBase", io.vertigo.dynamo.impl.database.Resources.values());
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

	/** {@inheritDoc} */
	public ConnectionProviderPlugin getConnectionProvider() {
		return connectionProviderPlugin;
	}

	/** {@inheritDoc} */
	public KCallableStatement createCallableStatement(final KConnection connection, final String procName) {
		return new KCallableStatementImpl(statementHandler, dataBaseListener, connection, procName);
	}

	/** {@inheritDoc} */
	public KPreparedStatement createPreparedStatement(final KConnection connection, final String sql, final boolean returnGeneratedKeys) {
		return new KPreparedStatementImpl(statementHandler, dataBaseListener, connection, sql, returnGeneratedKeys);

	}
}
