/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamox.task;

import java.util.Collections;
import java.util.List;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;

/**
 * Cette implémentation permet de créer la requête SQL bindée ainsi que de sortir la liste des paramètres de la requête (IN, OUT, IN/OUT).
 * @author pchretien
 */
final class SqlParserHandler implements ScriptParserHandler {
	/** Requête SQL fabriquée lors du parsing. */
	private final StringBuilder sql;
	/** Liste des paramètres. */
	private final List<TaskEngineSQLParam> params;

	SqlParserHandler() {
		sql = new StringBuilder();
		params = new java.util.ArrayList<>();
	}

	/** {@inheritDoc} */
	@Override
	public void onText(final String text) {
		appendSql(text);
	}

	/** {@inheritDoc} */
	@Override
	public void onExpression(final String expression, final ScriptSeparator separator) {
		//Cas d'un vrai paramètre.
		// Et on teste s'il s'agit d'un attribut du service.
		// Dans le cas des DTO on ne teste que le nom du DTO et non (pour l'instant) son paramètre

		final TaskEngineSQLParam param = new TaskEngineSQLParam(expression);
		params.add(param);
		//On binde paramètre, en le remplaçant par un "?"
		appendSql("?");
	}

	/**
	 * Création de la requête SQL lors du parsing.
	 * @param str String
	 */
	private void appendSql(final String str) {
		sql.append(str);
	}

	/**
	 * @return Liste des paramètres.
	 */
	List<TaskEngineSQLParam> getParams() {
		return Collections.unmodifiableList(params);
	}

	/**
	 * @return Requête SQL bindée (donc Utilisable en JDBC).
	 */
	String getSql() {
		return sql.toString();
	}
}
