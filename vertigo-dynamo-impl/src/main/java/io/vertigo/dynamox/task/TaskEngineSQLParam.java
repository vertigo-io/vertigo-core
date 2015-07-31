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

import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.lang.Assertion;

/**
 * Paramètres créés par l'analyseur et utilisés par le Handler.
 *
 * Ces paramètres sont de trois types :
 * - IN 	: le séparateur utilisé est #
 * - OUT 	: le séparateur utilisé est %
 * - IN OUT : le séparateur utilisé est @
 *
 * @author pchretien
 */
final class TaskEngineSQLParam {
	/**
	 * Liste énumérée des différents types de paramètre SQL Il existe trois types dont inout qui n'a pas d'équivalent en
	 * java pour les objets simples (non mutables).
	 */
	enum InOutType {
		/**
		 * Paramètre SQL de type IN.
		 */
		SQL_IN('#'),
		/**
		 * Paramètre SQL de type OUT.
		 */
		SQL_OUT('%'),
		/**
		 * Paramètre SQL de type IN OUT.
		 */
		SQL_INOUT('@');

		final char separator;

		InOutType(final char separator) {
			this.separator = separator;
		}

		/**
		 * Permet de connaitre le type du paramètre SQL en fonction du séparateur trouvé.
		 *
		 * @param separator Séparateur
		 * @return Type de paramètre SQL
		 */
		static SqlPreparedStatement.ParameterType getType(final char separator) {
			switch (separator) {
				case '#':
					return SqlPreparedStatement.ParameterType.IN;
				case '%':
					return SqlPreparedStatement.ParameterType.OUT;
				case '@':
					return SqlPreparedStatement.ParameterType.INOUT;
				default:
					throw new IllegalArgumentException(separator + " non reconnu");
			}
		}
	}

	// ==========================================================================
	// ==========================================================================
	private final String attributeName;
	private final String fieldName;
	private final Integer rowNumber;
	private final SqlPreparedStatement.ParameterType inOut;
	private int index = -1;

	/**
	 * Crée un objet Paramètre pour la requête.
	 *
	 * @param betweenCar String
	 * @param inOut Type du Parametre
	 */
	TaskEngineSQLParam(final String betweenCar, final SqlPreparedStatement.ParameterType inOut) {
		String newAttributeName = betweenCar;
		String newfieldName = null;
		Integer dtcRowNumber = null;

		final int indexOfFirstPoint = newAttributeName.indexOf('.');
		if (indexOfFirstPoint > -1) {
			final int indexOfLastPoint = newAttributeName.lastIndexOf('.');
			// cas du DTO/DTC
			// exemple : DTO_PERSONNE.NOM
			newAttributeName = newAttributeName.substring(0, indexOfFirstPoint);
			newfieldName = betweenCar.substring(indexOfLastPoint + 1);
			// cas particulier des DTC : il y a qqc entre le premier et le deuxieme point
			// qui doit être un entier >= 0
			// exemple : DTC_PERSONNE.12.Nom
			if (indexOfFirstPoint != indexOfLastPoint) {
				final String betweenPoints = betweenCar.substring(indexOfFirstPoint + 1, indexOfLastPoint);
				dtcRowNumber = parseDtcRowNumber(betweenCar, betweenPoints);
			}
		}
		//-----
		// Le paramètre n'est pas encore indexé
		Assertion.checkNotNull(newAttributeName);
		Assertion.checkNotNull(inOut);
		// Si le numéro de ligne est renseignée alors le champ doit l'être aussi
		Assertion.checkNotNull(dtcRowNumber == null || newfieldName != null);
		//-----
		attributeName = newAttributeName;
		this.inOut = inOut;
		fieldName = newfieldName;
		rowNumber = dtcRowNumber;
	}

	private static Integer parseDtcRowNumber(final String betweenCar, final String betweenPoints) {
		final Integer dtcRowNumber;
		try {
			dtcRowNumber = Integer.valueOf(betweenPoints);
		} catch (final NumberFormatException nfe) {
			throw new RuntimeException("Paramètre " + betweenCar + " incohérent : " + betweenPoints + " n'est pas un entier.", nfe);
		}
		if (dtcRowNumber == null || dtcRowNumber.intValue() < 0) {
			throw new RuntimeException("Paramètre " + betweenCar + " incohérent : " + betweenPoints + " doit être positif ou nul.");
		}
		return dtcRowNumber;
	}

	void setIndex(final int index) {
		this.index = index;
	}

	/**
	 * Un paramètre est primitif si il ne correspond pas à une DTC ou un DTO.
	 *
	 * @return S'il s'agit d'un paramètre primitif
	 */
	boolean isPrimitive() {
		return fieldName == null;
	}

	/**
	 * @return Paramètre de type liste.(DTC)
	 */
	boolean isList() {
		return !isPrimitive() && rowNumber != null;
	}

	/**
	 * @return Paramètre de type Objet.(DTO)
	 */
	boolean isObject() {
		return !isPrimitive() && rowNumber == null;
	}

	/**
	 * @return Nom de l'attribut de la tache (ou paramètre de tache)
	 */
	String getAttributeName() {
		return attributeName;
	}

	/**
	 * @return Nom du champ
	 */
	String getFieldName() {
		return fieldName;
	}

	/**
	 * @return Numéro de ligne dans le cas d'un paramètre représentant un élément d'une liste
	 */
	int getRowNumber() {
		Assertion.checkNotNull(rowNumber, "il ne s'agit pas d'une liste");
		//-----
		return rowNumber;
	}

	/**
	 * @return Type du paramètre
	 */
	SqlPreparedStatement.ParameterType getType() {
		return inOut;
	}

	/**
	 * @return Index de l'attribut
	 */
	int getIndex() {
		return index;
	}
}
