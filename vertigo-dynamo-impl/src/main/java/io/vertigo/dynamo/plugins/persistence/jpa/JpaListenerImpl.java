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
package io.vertigo.dynamo.plugins.persistence.jpa;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.lang.Assertion;

import org.apache.log4j.Logger;

/**
 * Implémentation standard du Listener de réception des événements produits par l'exécution des tachess.
 *
 * @author pchretien
 */
final class JpaListenerImpl {
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
	private static final Logger SQL_LOG = Logger.getLogger("Sql");

	private final AnalyticsManager analyticsManager;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager Analytics
	 */
	JpaListenerImpl(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	public void onStart(final String query) {
		if (SQL_LOG.isDebugEnabled()) {
			// on passe le preparedStatement en argument pour éviter de
			// construire la query si pas nécessaire
			SQL_LOG.debug("Execution du prepareStatement : " + query);
		}
		analyticsManager.getAgent().addMetaData(MD_DB_SQL, query);
	}

	public void onFinish(final String query, final boolean success, final long elapsedTime, final Long nbModifiedRow, final Long nbSelectedRow) {
		if (SQL_LOG.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder()
					.append("Execution du prepareStatement : ")
					.append(query);
			// on passe le preparedStatement en argument pour éviter de
			// construire la query si pas nécessaire
			if (success) {
				sb.append(" reussie en  ( ");
			} else {
				sb.append(" interrompue apres ( ");
			}
			sb.append(elapsedTime);
			sb.append(MS);
			if (nbModifiedRow != null) {
				sb.append(" ").append(nbModifiedRow);
				sb.append(nbModifiedRow > 1 ? " lignes modifiées" : " ligne modifiée");
			}
			if (nbSelectedRow != null) {
				sb.append(" ").append(nbSelectedRow);
				sb.append(nbSelectedRow > 1 ? " lignes récupérées" : " ligne récupérée");
			}
			SQL_LOG.info(sb.toString());
		}
		//On choisit d'incrémenter l'indicateur.
		//Se faisant on perd le moyen de faire la moyenne par requete,
		//Si le besoin apparaissait il faudrait creer un sous-process autour des appels
		analyticsManager.getAgent().incMeasure(ME_DB_TIME, elapsedTime);
		analyticsManager.getAgent().incMeasure(ME_DB_COMMAND_COUNT, nbModifiedRow != null ? 1 : 0);
		analyticsManager.getAgent().incMeasure(ME_DB_QUERY_COUNT, nbSelectedRow != null ? 1 : 0);
	}
}
