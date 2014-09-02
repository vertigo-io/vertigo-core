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
package io.vertigo.dynamox.domain.formatter;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.JsonExclude;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.impl.domain.metamodel.AbstractFormatterImpl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * Gestion des formatages de nombres.
 * L'argument est obligatoire, il permet de préciser le format d'affichage des nombres.
 * 
 * A l'affichage 
 * - le séparateur de millier est un espace 
 * - le séparateur décimal est une virgule
 * En saisie 
 * - les séparateurs de milliers acceptés sont l'espace et l'espace insécable
 * - les séparateurs décimaux acceptés  sont la virgule et le point
* 
 * Exemple d'argument : #,###,##0.00
  * 
 * @author pchretien
 */
public class FormatterNumber extends AbstractFormatterImpl {
	/**
	 * Format d'affichage des nombres.
	 */
	private String pattern;
	@JsonExclude
	private final DecimalFormatSymbols decFormatSymbols;

	/**
	 * Constructeur.
	 * @param name Nom du formatteur
	 */
	public FormatterNumber(final String name) {
		super(name);
		decFormatSymbols = new java.text.DecimalFormatSymbols();
		decFormatSymbols.setDecimalSeparator(','); //séparateur décimal
		decFormatSymbols.setGroupingSeparator(' '); //séparateur de milliers
	}

	/**
	 * @return Pattern utilisé
	 */
	public final String getPattern() {
		return pattern;
	}

	/** {@inheritDoc} */
	@Override
	public void initParameters(final String args) {
		Assertion.checkNotNull(args);
		//---------------------------------------------------------------------
		pattern = args;
		//----------------------------------------------------------------------
		//On vérifie la syntaxe de DecimalFormat
		Assertion.checkNotNull(new DecimalFormat(pattern));
	}

	/**
	 * @return Symboles decimaux utilisés
	 */
	protected DecimalFormatSymbols getDecimalFormatSymbols() {
		return decFormatSymbols;
	}

	/*
	 * Les formatters java ne sont pas threadSafe,
	 * on les recrée à chaque usage.
	 */
	private NumberFormat createNumberFormat() {
		// Si format non précisé on utilise le format par défaut
		return new DecimalFormat(pattern, getDecimalFormatSymbols());
	}

	private static void checkType(final DataType dataType) {
		Assertion.checkArgument(dataType == DataType.BigDecimal || dataType == DataType.Double || dataType == DataType.Integer || dataType == DataType.Long, "FormatterNumber ne s'applique qu'aux Nombres");
	}

	/** {@inheritDoc} */
	public final Object stringToValue(final String strValue, final DataType dataType) throws FormatterException {
		checkType(dataType);
		//----------------------------------------------------------------------
		//Pour les nombres on "trim" à droite et à gauche
		String sValue = StringUtil.isEmpty(strValue) ? null : strValue.trim();

		if (sValue == null) {
			return null;
		}

		try {
			final DecimalFormatSymbols decimalFormatSymbols = getDecimalFormatSymbols();
			/**
			 * Puis on transforme la chaine pour revenir à l'ecriture la plus simple.
			 * Cela pour utiliser le Number.valueOf plutot que le parse de NumberFormat.
			 */
			sValue = cleanStringNumber(sValue, decimalFormatSymbols);

			switch (dataType) {
				case BigDecimal:
					return new BigDecimal(sValue);
				case Double:
					return Double.valueOf(sValue);
				case Integer:
					return toInteger(sValue);
				case Long:
					return Long.valueOf(sValue);
				case Boolean:
				case DataStream:
				case Date:
				case DtList:
				case DtObject:
				case String:
					throw new RuntimeException("Type unsupported : " + dataType);
				default:
					throw new IllegalArgumentException("Type unknown : " + dataType);
			}
		} catch (final NumberFormatException e) {
			// cas des erreurs sur les formats de nombre
			throw new FormatterException(Resources.DYNAMOX_NUMBER_NOT_FORMATTED, e);
		}

	}

	private static Integer toInteger(final String sValue) {
		// on commence par vérifier que c'est bien un entier (Integer ou Long)
		Long.valueOf(sValue);
		try {
			// c'est bien un entier. On va vérifier qu'il s'agit bien d'un Integer
			return Integer.valueOf(sValue);
		} catch (final NumberFormatException e) {
			// C'est un entier trop grand
			final FormatterException formatterException = new FormatterException(Resources.DYNAMOX_NUMBER_TOO_BIG);
			formatterException.initCause(e);
			throw formatterException;
		}
	}

	/**
	 * Simplifie une chaine réprésentant un nombre.
	 * Utilisé en préprocessing avant le parsing. 
	 * @param value Chaine saisie
	 * @param decimalFormatSymbols symboles décimaux utilisées
	 * @return Chaine simplifiée
	 */
	protected String cleanStringNumber(final String value, final DecimalFormatSymbols decimalFormatSymbols) {
		return cleanStringNumber(value, decimalFormatSymbols.getDecimalSeparator(), decimalFormatSymbols.getGroupingSeparator());
	}

	/**
	 * Simplifie une chaine réprésentant un nombre.
	 * Utilisé en préprocessing avant le parsing. 
	 * @param sValue Chaine saisie
	 * @param decimalCharUsed caractère décimal utilisé
	 * @param groupCharUsed caractère de millier utilisé
	 * @return Chaine simplifiée
	 */
	protected static final String cleanStringNumber(final String sValue, final char decimalCharUsed, final char groupCharUsed) {
		String result = sValue;
		// 1 >> On supprime les blancs. (simples et insécables)
		if (groupCharUsed == ' ' || groupCharUsed == (char) 160) {
			result = result.replace((char) 160, ' '); //aussi rapide que l'indexOf si absend
			result = StringUtil.replace(result, " ", "");
		} else if (result.indexOf(groupCharUsed) != -1) {
			// 2 >> On supprime les séparateurs de milliers.
			result = StringUtil.replace(result, String.valueOf(groupCharUsed), "");
		}

		// 3 >> On remplace le séparateur décimal par des '.'
		result = result.replace(decimalCharUsed, '.');
		return result;
	}

	/** {@inheritDoc} */
	public final String valueToString(final Object objValue, final DataType dataType) {
		checkType(dataType);
		//----------------------------------------------------------------------
		String decimalString = null;
		if (objValue == null) {
			decimalString = "";
		} else {
			switch (dataType) {
				case BigDecimal:
				case Double:
					decimalString = createNumberFormat().format(objValue);
					break;
				case Integer:
				case Long:
					if (pattern == null) {
						// on ne passe surtout pas pas un formatter interne java
						// pour les perfs et conserver des identifiants en un seul morceau
						decimalString = objValue.toString();
					} else {
						decimalString = createNumberFormat().format(objValue);
					}
					break;
				case Boolean:
				case DataStream:
				case Date:
				case DtList:
				case DtObject:
				case String:
					throw new RuntimeException("Type unsupported" + dataType);
				default:
					throw new IllegalArgumentException("Type unknown : " + dataType);
			}
		}
		return decimalString;
	}
}
