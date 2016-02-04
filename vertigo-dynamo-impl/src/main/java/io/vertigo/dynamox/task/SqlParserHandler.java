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
package io.vertigo.dynamox.task;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamox.task.TaskEngineSQLParam.InOutType;
import io.vertigo.lang.Assertion;

import java.util.Collections;
import java.util.List;

/**
 * Cette implémentation permet de créer la requête SQL bindée ainsi que de sortir la liste des paramètres de la requête (IN, OUT, IN/OUT).
 * @author pchretien
 */
final class SqlParserHandler implements ScriptParserHandler {
	private final TaskDefinition taskDefinition;
	/** Requête SQL fabriquée lors du parsing. */
	private final StringBuilder sql;
	/** Liste des paramètres. */
	private final List<TaskEngineSQLParam> params;

	SqlParserHandler(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//-----
		this.taskDefinition = taskDefinition;
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

		final TaskEngineSQLParam param = new TaskEngineSQLParam(expression, InOutType.isIn(separator.getSeparator()));
		addParam(param);
		//On binde paramètre, en le remplaçant par un "?"
		appendSql("?");
	}

	/**
	 * Vérifie qu'un nom de champ pour un attribut de type DTX existe
	 * @param taskAttribute TaskAttribute
	 * @param fieldName Nom du champ dont il faut vérifier l'existence
	 */
	private static void checkFieldName(final TaskAttribute taskAttribute, final String fieldName) {
		//Dans le cas des domaines de type DTO et DTC génériques,
		//c'est à dire ne précisant pas un DT,
		//il n'est pas possible d'eefectuer de vérification au niveau modèle.
		if (taskAttribute.getDomain().hasDtDefinition()) {
			//la méthode getDtDefinition() possède une assertion qui vérifie
			// que l'on est sur un domaine gérant les type complexes (DTO ou DTC)
			final DtField dtField = taskAttribute.getDomain().getDtDefinition().getField(fieldName);
			Assertion.checkNotNull(dtField);
		}
	}

	/**
	 * Création de la requête SQL lors du parsing.
	 * @param str String
	 */
	private void appendSql(final String str) {
		sql.append(str);
	}

	private void addParam(final TaskEngineSQLParam param) {
		//On vérifie la cohérence du Parmètre fourni
		//On vérifie que l'attribut existe
		final TaskAttribute taskAttribute = taskDefinition.getInAttribute(param.getAttributeName());
		//======================================================================
		if (param.getFieldName() != null) {
			// On vérifie que le fieldName existe pour l'attribut précisé
			checkFieldName(taskAttribute, param.getFieldName());
		}

		//Ajout dans la liste et maj de l'index.
		param.setIndex(params.size());
		params.add(param);
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
