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

import io.vertigo.commons.script.ExpressionParameter;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.script.SeparatorType;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simule le mécanisme JSP sur un fichier texte quelconque.
 * Remplace les éléments compris entre les séparateurs par une évaluation dynamique.
 * <% %>  : permet d'insérer des blocs java
 * <%= %> : permet d'ajouter des éléments au texte
 *
 * @author  pchretien
 */
final class ScriptPreProcessor {
	private final ScriptManager scriptManager;
	private final Map<TaskAttribute, Object> parameterValuesMap;
	private final SeparatorType separatorType;

	/**
	 * Constructeur.
	 * @param parameterValuesMap Map des paramètres
	 * @param separatorType Type de preprocessing CLASSIC ou HTML
	 */
	ScriptPreProcessor(final ScriptManager scriptManager, final Map<TaskAttribute, Object> parameterValuesMap, final SeparatorType separatorType) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(parameterValuesMap);
		Assertion.checkNotNull(separatorType);
		//---------------------------------------------------------------------
		this.scriptManager = scriptManager;
		this.parameterValuesMap = parameterValuesMap;
		this.separatorType = separatorType;
	}

	private static List<ExpressionParameter> createParameters(final ScriptManager scriptManager, final Map<TaskAttribute, Object> parameterValuesMap) {
		Assertion.checkNotNull(scriptManager);
		Assertion.checkNotNull(parameterValuesMap);
		//---------------------------------------------------------------------
		final List<ExpressionParameter> tmpParameters = new ArrayList<>(parameterValuesMap.size());

		// ---------Initialisation des types et noms de paramètre------------
		ExpressionParameter scriptEvaluatorParameter;
		for (final Entry<TaskAttribute, Object> entry : parameterValuesMap.entrySet()) {
			final Class<?> clazz;
			final TaskAttribute taskAttribute = entry.getKey();
			final Domain domain = taskAttribute.getDomain();
			if (domain.getDataType().isPrimitive()) {
				// Pour les types primitifs
				clazz = domain.getDataType().getJavaClass();
			} else if (domain.getDataType() == DataType.DtList) {
				// Pour les types liste
				clazz = DtList.class;
			} else if (domain.getDataType() == DataType.DtObject) {
				// Pour les types composites
				if (domain.hasDtDefinition()) {
					clazz = ClassUtil.classForName(domain.getDtDefinition().getClassCanonicalName());
				} else {
					//si l'objet est dynamique on le laisse en dtObject
					clazz = DtObject.class;
				}
			} else {
				throw new RuntimeException("Type de paramètre non géré " + taskAttribute.getName() + ":" + domain.getName());
			}
			scriptEvaluatorParameter = new ExpressionParameter(StringUtil.constToCamelCase(taskAttribute.getName(), false), clazz, entry.getValue());
			tmpParameters.add(scriptEvaluatorParameter);
		}
		return tmpParameters;
	}

	String evaluate(final String query) {
		//On commence par vérifier si le preprocessor s'applique.
		if (containsSeparator(query, separatorType.getSeparators())) {
			//Evaluation de la query à la mode JSP avec les paramètres passés au démarrage.
			return scriptManager.evaluateScript(query, separatorType, createParameters(scriptManager, parameterValuesMap));
		}
		return query;
	}

	private static boolean containsSeparator(final String query, final List<ScriptSeparator> separators) {
		for (final ScriptSeparator separator : separators) {
			if (query.contains(separator.getBeginSeparator()) && query.contains(separator.getEndSeparator())) {
				return true;
			}
		}
		return false;
	}
}
