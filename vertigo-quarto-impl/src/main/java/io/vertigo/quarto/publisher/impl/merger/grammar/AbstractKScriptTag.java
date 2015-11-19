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
package io.vertigo.quarto.publisher.impl.merger.grammar;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.quarto.publisher.model.PublisherNode;
import io.vertigo.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe abstraite représentant un KScriptTag.
 *
 * @author pchretien, npiedeloup
 */
abstract class AbstractKScriptTag {
	/**
	 * JSP start bloc.
	 */
	protected static final String START_BLOC_JSP = "&lt;%";
	/**
	 * END_BLOC_JSP.
	 */
	protected static final String END_BLOC_JSP = "%&gt;";
	/**
	 * FIELD_PATH_CALL.
	 */
	protected static final String FIELD_PATH_CALL = "^([0-9a-zA-Z_]+(?:\\.[0-9a-zA-Z_]+)*)";
	/**
	 * FIELD_PATH_CALL_CONDITIONAL.
	 * ADU - 20120529 : modification du pattern pour acceptation espaces et accents.
	 */
	protected static final String FIELD_PATH_CALL_EQUALS_CONDITION = "^([0-9a-zA-Z_]+(?:\\.[0-9a-zA-Z_]+)*)=(\\&quot;|\")(.*)(\\&quot;|\")";

	/**
	 * Ajouter l'appel de la methode getStringValue sur un fieldPath.
	 *
	 * @param fieldPath fieldPath du champ recherché
	 * @param currentVariableName nom de la variable local courante
	 * @return Appel de la methode report
	 */
	protected static final String getCallForFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getString(\"" + fieldPath + "\")";
	}

	/**
	 * Ajouter l'appel de la methode getBooleanValue sur un fieldPath.
	 *
	 * @param fieldPath fieldPath du champ recherché
	 * @param currentVariableName nom de la variable local courante
	 * @return Appel de la methode report
	 */
	protected static final String getCallForBooleanFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getBoolean(\"" + fieldPath + "\")";
	}

	/**
	 * Ajouter du test equals d'un fieldPath et d'une valeur fixe du modèle.
	 *
	 * @param fieldPath Chemin du champ.
	 * @param value Valeur à tester.
	 * @param currentVariableName Nom de la variable courante
	 * @return Code java resultant.
	 */
	protected static final String getCallForEqualsBooleanFieldPath(final String fieldPath, final String value, final String currentVariableName) {
		return new StringBuilder()
				.append(getCallForFieldPath(fieldPath, currentVariableName))
				.append(".equals(\"")
				.append(value)
				.append("\")")
				.toString();
	}

	/**
	 * Ajouter l'appel de la methode getNodes sur un fieldPath.
	 * @param fieldPath Chemin du champ.
	 * @param currentVariableName Nom de la variable courante
	 * @return Code java resultant.
	 */
	protected static final String getCallForCollectionFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getNodes(\"" + fieldPath + "\")";
	}

	/**
	 * Ajouter l'appel de la methode getNode sur un fieldPath.
	 * @param fieldPath Chemin du champ.
	 * @param currentVariableName Nom de la variable courante
	 * @return Code java resultant.
	 */
	protected static final String getCallForObjectFieldPath(final String fieldPath, final String currentVariableName) {
		return currentVariableName + ".getNode(\"" + fieldPath + "\")";
	}

	/**
	 * @return Class d'accès aux données.
	 */
	protected static final Class<?> getDataAccessorClass() {
		return PublisherNode.class;
	}

	/**
	 * Injecte les données dans la chaine de caractere de rendu d'un tag.
	 *
	 * @param tagRepresentation la chaine represantant un tag donné
	 * @param datas Données sous forme de chaines de caractere
	 * @return Tag java représenté sous forme de chaine de caractere
	 */
	protected static final String getTagRepresentation(final String tagRepresentation, final String[] datas) {
		return START_BLOC_JSP + StringUtil.format(tagRepresentation, (Object[]) datas) + END_BLOC_JSP;
	}

	/**
	 * Permet de vérifier le format d'un attribut de tag et de le parser.
	 *
	 * @param attribute l'attribut d'un tag
	 * @param regEexpFormat le format dans lequel il doit etre ecrit
	 * @return la list des groupe de l'expression reguliere(chaine entre
	 *         parentheses dans l'expression reguliere) ou null si cela ne
	 *         matche pas le format. la premiere case du tableau correspond
	 *         toujours a la chaine à l'attribut lui même
	 */
	protected static final String[] parseAttribute(final String attribute, final String regEexpFormat) {
		Assertion.checkNotNull(attribute);
		Assertion.checkNotNull(regEexpFormat);
		//-----

		final String[] groups;
		int nbGroup = 0;

		final Pattern pattern = Pattern.compile(regEexpFormat);
		final Matcher matcher = pattern.matcher(attribute);

		if (!matcher.matches()) {
			throw new VSystemException(StringUtil.format("attribut \'{0}\' mal forme (ne respect pas le format {1})", attribute, regEexpFormat));
		}

		nbGroup = matcher.groupCount();
		if (nbGroup > 0) {
			groups = new String[nbGroup + 1];

			for (int i = 0; i <= nbGroup; i++) {
				groups[i] = matcher.group(i);
			}
		} else {
			groups = new String[1];
			groups[0] = attribute;
		}

		return groups;
	}

}
