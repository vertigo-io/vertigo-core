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
package io.vertigo.commons.script;

import io.vertigo.commons.script.parser.ScriptParserHandler;
import io.vertigo.commons.script.parser.ScriptSeparator;
import io.vertigo.lang.Manager;

import java.util.List;

/** 
 * Gestion des manipulations sur des scripts.
 *
 * @author pchretien
 */
public interface ScriptManager extends Manager {
	/**
	 * Parse le script, notifie le handler.
	 * La grammaire est constituées de simples balises (Séparateurs). 
	 *
	 * @param script Script à analyser
	 * @param scriptHandler Handler gérant la grammaire analysée
	 * @param separators Liste des séparateurs autorisés dans la grammaire.
	 */
	void parse(final String script, final ScriptParserHandler scriptHandler, final List<ScriptSeparator> separators);

	/**
	 * Evaluation du script.
	 * Transforme un script en text.
	 * @param script Script à évaluer
	 * @return Script évalué
	 */
	String evaluateScript(final String script, final SeparatorType separatorType, final List<ExpressionParameter> parameters);

	/**
	 * Evaluation d'une expression et non d'un bloc de code.
	 *  Exemple d'expressions exprimées en java 
	 *  - name  
	 *  - birthDate
	 *  - age>20
	 *  - salary>5000 && age <30
	 *  - name + surName
	 * @param expression Expression
	 * @param parameters Paramètres
	 * @param type Type retourné
	 * @return Résultat de l'expression après évaluation
	 */
	<J> J evaluateExpression(final String expression, List<ExpressionParameter> parameters, Class<J> type);
}
