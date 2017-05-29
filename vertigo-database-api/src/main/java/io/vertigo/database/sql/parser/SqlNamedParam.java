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
package io.vertigo.database.sql.parser;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Paramètres créés par l'analyseur et utilisés par le Handler.
 *
 * - IN 	: le séparateur utilisé est #
 *
 * @author pchretien
 */
public final class SqlNamedParam {
	private final String[] tokens;
	private final String attributeName;
	private final String fieldName;
	private final Integer rowNumber;

	/**
	 * Constructor.
	 *
	 * @param betweenCar String
	 */
	public SqlNamedParam(final String betweenCar) {
		tokens = betweenCar.split("\\.");
		if (tokens.length == 1) {
			//Simple bound param : CODE
			attributeName = tokens[0];
			fieldName = null;
			rowNumber = null;
		} else if (tokens.length == 2) {
			// a field of a bean is bound
			// example : DTO_MOVIE.TITLE
			attributeName = tokens[0];
			fieldName = tokens[1];
			rowNumber = null;
		} else if (tokens.length == 3) {
			// cas particulier des DTC : il y a qqc entre le premier et le deuxieme point
			// qui doit être un entier >= 0
			// exemple : DTC_PERSONNE.12.Nom
			attributeName = tokens[0];
			fieldName = tokens[2];
			rowNumber = parseDtcRowNumber(tokens[1]);
		} else {
			throw new IllegalStateException();
		}
		Assertion.checkNotNull(attributeName);
		//Si le numéro de ligne est renseignée alors le champ doit l'être aussi
		Assertion.when(rowNumber != null)
				.check(() -> fieldName != null, "Invalid syntax for field in DTC. Use : MY_DTO.0.MY_FIELD");
	}

	private static int parseDtcRowNumber(final String betweenPoints) {
		try {
			final Integer dtcRowNumber = Integer.valueOf(betweenPoints);
			Assertion.checkState(dtcRowNumber != null && dtcRowNumber >= 0, "Paramètre incohérent : {0} doit être positif ou null.", betweenPoints);
			return dtcRowNumber;
		} catch (final NumberFormatException nfe) {
			throw WrappedException.wrap(nfe, betweenPoints + " n'est pas un entier.");
		}
	}

	/**
	 * Un paramètre est primitif si il ne correspond pas à une DTC ou un DTO.
	 *
	 * @return S'il s'agit d'un paramètre primitif
	 */
	public boolean isPrimitive() {
		return fieldName == null;
	}

	/**
	 * @return Paramètre de type liste.(DTC)
	 */
	public boolean isList() {
		return rowNumber != null && !isPrimitive();
	}

	/**
	 * @return Paramètre de type Objet.(DTO)
	 */
	public boolean isObject() {
		return rowNumber == null && !isPrimitive();
	}

	/**
	 * @return Nom de l'attribut de la tache (ou paramètre de tache)
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @return Nom du champ
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return Numéro de ligne dans le cas d'un paramètre représentant un élément d'une liste
	 */
	public int getRowNumber() {
		Assertion.checkNotNull(rowNumber, "il ne s'agit pas d'une liste");
		//-----
		return rowNumber;
	}
}
