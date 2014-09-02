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
package io.vertigo.dynamo.impl.database.vendor.core;

import io.vertigo.core.exception.VUserException;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.MessageKey;
import io.vertigo.core.lang.MessageText;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.database.vendor.SqlExceptionHandler;
import io.vertigo.dynamo.impl.database.Resources;

import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * Handler abstrait des exceptions SQL qui peuvent survenir dans un service.
 * Cette classe est abstraite et doit être concretisée par une sous-classe. 
 * Dans le cas d'une contrainte d'intégrité référentiel ou d'unicité, le message de
 * l'erreur utilisateur doit être specialisé en l'ajoutant comme ressource dans
 * le RessourceManager en utilisant le nom de la contrainte comme clef.
 *
 * @author npiedeloup, evernat
 */
public abstract class AbstractSQLExceptionHandler implements SqlExceptionHandler {
	private final Logger logger = Logger.getLogger(getClass()); //pas static, car on est sur une class abstraite

	/**
	 * @param msg Message base de données
	 * @return Code de la contrainte
	 */
	protected abstract String extractConstraintName(final String msg);

	/**
	 * @param sqle Exception base de données
	 */
	protected void handleTooLargeValueSqlException(final SQLException sqle) {
		final MessageKey key = Resources.DYNAMO_SQL_CONSTRAINT_TOO_BIG_VALUE;
		logger.warn(new MessageText(key).getDisplay(), sqle);
		//On se contente de logger l'exception cause mais on ne la lie pas à l'erreur utilisateur.
		throw new VUserException(new MessageText(key));
	}

	protected void handleUserSQLException(final SQLException sqle) {
		String msg = sqle.getMessage();
		final int i1 = msg.indexOf("<text>");
		final int i2 = msg.indexOf("</text>", i1);

		if (i1 > -1 && i2 > -1) {
			msg = msg.substring(i1 + 6, i2);
		}
		//On se contente de logger l'exception cause mais on ne la lie pas à l'erreur utilisateur.
		throw new VUserException(new MessageText(msg, null));
	}

	/**
	 * Traite l'exception lié à la contrainte d'intégrité.
	 * Et lance une KUserException avec le message par défaut passé en paramètre et une MessageKey basé sur le nom de la contrainte. 
	 * @param sqle Exception SQL
	 * @param defaultMsg Message par defaut
	 */
	protected final void handleConstraintSQLException(final SQLException sqle, final MessageKey defaultMsg) {
		final String msg = sqle.getMessage();
		// recherche le nom de la contrainte d'intégrité
		final String constraintName = extractConstraintName(msg);
		Assertion.checkNotNull(constraintName, "Impossible d''extraire le nom de la contrainte : {0}", msg);

		// recherche le message pour l'utilisateur
		//On crée une clé de MessageText dynamiquement sur le nom de la contrainte d'intégrité
		//Ex: CK_PERSON_FULL_NAME_UNIQUE
		final MessageKey constraintKey = new MessageKey() {
			private static final long serialVersionUID = -3457399434625437700L;

			/** {@inheritDoc} */
			public String name() {
				return constraintName;
			}
		};

		//On récupère ici le message externalisé par défaut : Resources.DYNAMO_SQL_CONSTRAINT_IMPOSSIBLE_TO_DELETE ou Resources.DYNAMO_SQL_CONSTRAINT_ALREADY_REGISTRED) 
		final String defaultConstraintMsg = new MessageText(defaultMsg).getDisplay();
		final MessageText userContraintMessageText = new MessageText(defaultConstraintMsg, constraintKey);
		final VUserException constraintException = new VUserException(userContraintMessageText);
		constraintException.initCause(sqle);
		throw constraintException;
	}

	protected void handleForeignConstraintSQLException(final SQLException sqle) {
		handleConstraintSQLException(sqle, Resources.DYNAMO_SQL_CONSTRAINT_IMPOSSIBLE_TO_DELETE);
	}

	protected void handleUniqueConstraintSQLException(final SQLException sqle) {
		handleConstraintSQLException(sqle, Resources.DYNAMO_SQL_CONSTRAINT_ALREADY_REGISTRED);
	}

	protected void handleOtherSQLException(final SQLException sqle, final KPreparedStatement statement) {
		final int errCode = sqle.getErrorCode();
		throw new RuntimeException(StringUtil.format("[Erreur SQL] {0} : {1}", errCode, statement != null ? statement.toString() : null), sqle);
	}
}
