package io.vertigo.dynamo.plugins.persistence.jpa;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.kernel.lang.Assertion;

import org.apache.log4j.Logger;

/**
 * Implémentation standard du Listener de réception des événements produits par l'exécution des tachess.
 * 
 * @author pchretien
 * @version $Id: JpaListenerImpl.java,v 1.1 2014/01/31 17:21:08 npiedeloup Exp $
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
	private final Logger sqlLog;

	private final AnalyticsManager analyticsManager;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager Analytics
	 */
	JpaListenerImpl(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//---------------------------------------------------------------------
		this.analyticsManager = analyticsManager;
		sqlLog = Logger.getLogger("Sql");
	}

	/** {@inheritDoc} */
	public void onStart(final String query) {
		if (sqlLog.isDebugEnabled()) {
			// on passe le preparedStatement en argument pour éviter de
			// construire la query si pas nécessaire
			sqlLog.debug("Execution du prepareStatement : " + query);
		}
		analyticsManager.getAgent().addMetaData(MD_DB_SQL, query);
	}

	/** {@inheritDoc} */
	public void onFinish(final String query, final boolean success, final long elapsedTime, final Long nbModifiedRow, final Long nbSelectedRow) {
		if (sqlLog.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Execution du prepareStatement : ");
			sb.append(query);
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
			sqlLog.info(sb.toString());
		}
		//On choisit d'incrémenter l'indicateur.
		//Se faisant on perd le moyen de faire la moyenne par requete, 
		//Si le besoin apparaissait il faudrait creer un sous-process autour des appels
		analyticsManager.getAgent().incMeasure(ME_DB_TIME, elapsedTime);
		analyticsManager.getAgent().incMeasure(ME_DB_COMMAND_COUNT, nbModifiedRow != null ? 1 : 0);
		analyticsManager.getAgent().incMeasure(ME_DB_QUERY_COUNT, nbSelectedRow != null ? 1 : 0);
	}
}
