package io.vertigo.dynamo.impl.database;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.statement.StatementStats;
import io.vertigo.kernel.lang.Assertion;

import org.apache.log4j.Logger;

/**
 * Implémentation standard du Listener de réception des événements produits par l'exécution des tachess.
 * 
 * @author pchretien
 * @version $Id: DataBaseListenerImpl.java,v 1.3 2013/10/22 12:24:21 pchretien Exp $
 */
final class DataBaseListenerImpl implements DataBaseListener {
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
	DataBaseListenerImpl(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//---------------------------------------------------------------------
		this.analyticsManager = analyticsManager;
		sqlLog = Logger.getLogger("Sql");
	}

	/** {@inheritDoc} */
	public void onPreparedStatementStart(final KPreparedStatement preparedStatement) {
		if (sqlLog.isDebugEnabled()) {
			// on passe le preparedStatement en argument pour éviter de
			// construire la query si pas nécessaire
			sqlLog.debug("Execution du prepareStatement : " + preparedStatement.toString());
		}
		analyticsManager.getAgent().addMetaData(MD_DB_SQL, preparedStatement.toString());
	}

	/** {@inheritDoc} */
	public void onPreparedStatementFinish(final StatementStats statementStats) {
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
