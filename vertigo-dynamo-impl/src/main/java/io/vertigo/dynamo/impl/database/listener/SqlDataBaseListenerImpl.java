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
package io.vertigo.dynamo.impl.database.listener;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.impl.database.statement.SqlStatementStats;

import org.apache.log4j.Logger;

/**
 * Implémentation standard du Listener de réception des événements produits par l'exécution des tachess.
 * 
 * @author pchretien
 */
public final class SqlDataBaseListenerImpl implements SqlDataBaseListener {
	/** ProcessMetaData Requete SQL */
	private static final String MD_DB_SQL = "DB_SQL";
	/** ProcessMeasure Temps base de données */
	private static final String ME_DB_TIME = "DB_TIME";
	/** ProcessMeasure Nombre d'accès de type command */
	private static final String ME_DB_COMMAND_COUNT = "DB_COMMAND_COUNT";
	/** ProcessMeasure Nombre d'accès de type query */
	private static final String ME_DB_QUERY_COUNT = "DB_QUERY_COUNT";

	private static final String MS = " ms)";

	/** Mécanisme de log utilisé pour le sql. */
	private final Logger sqlLog;

	private final AnalyticsManager analyticsManager;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager Analytics
	 */
	public SqlDataBaseListenerImpl(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//---------------------------------------------------------------------
		this.analyticsManager = analyticsManager;
		sqlLog = Logger.getLogger("Sql");
	}

	/** {@inheritDoc} */
	public void onPreparedStatementStart(final SqlPreparedStatement preparedStatement) {
		if (sqlLog.isDebugEnabled()) {
			// on passe le preparedStatement en argument pour éviter de
			// construire la query si pas nécessaire
			sqlLog.debug("Execution du prepareStatement : " + preparedStatement.toString());
		}
		analyticsManager.getAgent().addMetaData(MD_DB_SQL, preparedStatement.toString());
	}

	/** {@inheritDoc} */
	public void onPreparedStatementFinish(final SqlStatementStats statementStats) {
		if (sqlLog.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Execution du prepareStatement : ");
			sb.append(statementStats.getPreparedStatement().toString());
			// on passe le preparedStatement en argument pour éviter de
			// construire la query si pas nécessaire
			if (statementStats.isSuccess()) {
				sb.append(" reussie en  ( ");
			} else {
				sb.append(" interrompue apres ( ");
			}
			sb.append(statementStats.getElapsedTime());
			sb.append(MS);
			if (statementStats.getNbModifiedRow() != null) {
				final long nbModifiedRow = statementStats.getNbModifiedRow();
				sb.append(" ").append(nbModifiedRow);
				sb.append(nbModifiedRow > 1 ? " lignes modifiées" : " ligne modifiée");
			}
			if (statementStats.getNbSelectedRow() != null) {
				final long nbSelectedRow = statementStats.getNbSelectedRow();
				sb.append(" ").append(nbSelectedRow);
				sb.append(nbSelectedRow > 1 ? " lignes récupérées" : " ligne récupérée");
			}
			sqlLog.info(sb.toString());
		}
		//On choisit d'incrémenter l'indicateur.
		//Se faisant on perd le moyen de faire la moyenne par requete, 
		//Si le besoin apparaissait il faudrait creer un sous-process autour des appels
		analyticsManager.getAgent().incMeasure(ME_DB_TIME, statementStats.getElapsedTime());
		analyticsManager.getAgent().incMeasure(ME_DB_COMMAND_COUNT, statementStats.getNbModifiedRow() != null ? 1 : 0);
		analyticsManager.getAgent().incMeasure(ME_DB_QUERY_COUNT, statementStats.getNbSelectedRow() != null ? 1 : 0);
	}
}
