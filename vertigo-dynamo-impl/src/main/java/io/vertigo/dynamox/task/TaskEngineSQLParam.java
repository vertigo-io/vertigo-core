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

import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.WrappedException;

/**
 * Paramètres créés par l'analyseur et utilisés par le Handler.
 *
 * Ces paramètres sont de trois types :
 * - IN 	: le séparateur utilisé est #
 * - OUT 	: le séparateur utilisé est %
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
		SQL_OUT('%');

		private final char separator;

		InOutType(final char separator) {
			this.separator = separator;
		}

		char getSeparator() {
			return separator;
		}

		/**
		 * Permet de connaitre le type du paramètre SQL en fonction du séparateur trouvé.
		 *
		 * @param separator Séparateur
		 * @return Type de paramètre SQL
		 */
		static boolean isIn(final char separator) {
			switch (separator) {
				case '#':
					//IN
					return true;
				case '%':
					//OUT
					return false;
				default:
					throw new IllegalArgumentException(separator + " non reconnu");
			}
		}
	}

	private final String attributeName;
	private final String fieldName;
	private final Integer rowNumber;
	private final boolean in;
	private int index = -1;

	/**
	 * Constructor.
	 *
	 * @param betweenCar String
	 * @param in If the param is in
	 */
	TaskEngineSQLParam(final String betweenCar, final boolean in) {
		final String newAttributeName;
		final String newfieldName;
		final Integer dtcRowNumber;

		final int indexOfFirstPoint = betweenCar.indexOf('.');
		if (indexOfFirstPoint > -1) {
			final int indexOfLastPoint = betweenCar.lastIndexOf('.');
			// cas du DTO/DTC
			// exemple : DTO_PERSONNE.NOM
			newAttributeName = betweenCar.substring(0, indexOfFirstPoint);
			newfieldName = betweenCar.substring(indexOfLastPoint + 1);

			if (indexOfFirstPoint != indexOfLastPoint) {
				// cas particulier des DTC : il y a qqc entre le premier et le deuxieme point
				// qui doit être un entier >= 0
				// exemple : DTC_PERSONNE.12.Nom
				final String betweenPoints = betweenCar.substring(indexOfFirstPoint + 1, indexOfLastPoint);
				dtcRowNumber = parseDtcRowNumber(betweenCar, betweenPoints);
			} else {
				dtcRowNumber = null;
			}
		} else {
			//Cas du input natif (ex: CODE)
			newAttributeName = betweenCar;
			dtcRowNumber = null;
			newfieldName = null;
		}

		//-----
		// Le paramètre n'est pas encore indexé
		Assertion.checkNotNull(newAttributeName);
		// Si le numéro de ligne est renseignée alors le champ doit l'être aussi
		Assertion.when(dtcRowNumber != null).check(() -> newfieldName != null, "Invalid syntax for field in DTC. Use : MY_DTO.0.MY_FIELD");
		//-----
		attributeName = newAttributeName;
		this.in = in;
		fieldName = newfieldName;
		rowNumber = dtcRowNumber;
	}

	private static int parseDtcRowNumber(final String betweenCar, final String betweenPoints) {
		final Integer dtcRowNumber;
		try {
			dtcRowNumber = Integer.valueOf(betweenPoints);
		} catch (final NumberFormatException nfe) {
			throw WrappedException.wrap(nfe, "Paramètre " + betweenCar + " incohérent : " + betweenPoints + " n'est pas un entier.");
		}
		if (dtcRowNumber == null || dtcRowNumber < 0) {
			throw new VSystemException("Paramètre {0} incohérent : {1} doit être positif ou null.", betweenCar, betweenPoints);
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
		return rowNumber != null && !isPrimitive();
	}

	/**
	 * @return Paramètre de type Objet.(DTO)
	 */
	boolean isObject() {
		return rowNumber == null && !isPrimitive();
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

	boolean isIn() {
		return in;
	}

	/**
	 * @return Index de l'attribut
	 */
	int getIndex() {
		return index;
	}
}
