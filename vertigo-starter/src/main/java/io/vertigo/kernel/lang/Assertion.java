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
package io.vertigo.kernel.lang;

import io.vertigo.kernel.util.StringUtil;

/**
 * Permet de gérer les Assertions. 
 * C'est à dire les : 
 * <ul>
 * 	<li>pré conditions : validation des arguments</li>
 * 	<li>invariants / post conditions : validation des états</li>
 * </ul>
 * Ces notions ont été introduites avec le langage Eiffel par B. Meyer. Elles sont relatives à la notion de contrat.
 * Il s'agit de vérifier
 * les contrats en entrée <b>-précondition-</b>
 * les contrats en sortie <b>-postcondition-</b>
 * les conditions obligatoirement vérifiées à l'intérieur des méthodes <b>-invariant-</b>.
 * <br>Pour aller plus loin lire articles et ouvrages autour des travaux de B. Meyer et M. Fowler concernant la programmation par contrat (design by contract).
 *
 * @author fconstantin
 */
public final class Assertion {
	//-------------------------------------------------------------------------
	//-------------------------NullPointerException----------------------------
	//-------------------------------------------------------------------------

	/**
	 * Permet de tester le caractère obligatoire (non null) d'un objet.
	 * @param o Object Objet obligatoire
	 */
	public static void checkNotNull(final Object o) {
		if (o == null) {
			// Optimisé pour message sans formattage
			throw new NullPointerException();
		}
	}

	/**
	 * Permet de tester une erreur possible, ou le développeur à déjà tester la nullité de l'objet et appel checkNotNull tout de même.
	 * @param o Object Objet obligatoire
	 * @param msg Message d'erreur
	 * @param params paramètres du message
	 * @deprecated Vous n'utilisez certainement pas la bonne assertion, utiliser checkArgument à la place
	 */
	@Deprecated
	public static void checkNotNull(final boolean o, final String msg, final Object... params) {
		throw new IllegalArgumentException("Null assertion must be on Objet instance. You may have already checked nullity, use checkArgument instead.");
	}

	/**
	 * Permet de tester le caractère obligatoire (non null) d'un Boolean.
	 * @param o Object Objet obligatoire
	 * @param msg Message d'erreur
	 * @param params paramètres du message
	 */
	public static void checkBooleanNotNull(final Boolean o, final String msg, final Object... params) {
		checkNotNull(o, msg, params);
	}

	/**
	 * Permet de tester le caractère obligatoire (non null) d'un objet.
	 * @param o Object Objet obligatoire
	 * @param msg Message d'erreur
	 * @param params paramètres du message
	 */
	public static void checkNotNull(final Object o, final String msg, final Object... params) {
		if (o == null) {
			throw new NullPointerException(StringUtil.format(msg, params));
		}
	}

	//-------------------------------------------------------------------------
	//-------------------------IllegalArgumentException------------------------
	//-------------------------------------------------------------------------
	/**
	 * Permet de tester les arguments.
	 * Utilisé comme validation des préconditions.
	 * @param test Expression booléenne qui doit être vérifiée
	 * @param msg Message affiché si le test <b>n'est pas</b> vérifié.
	 * @param params paramètres du message
	 */
	public static void checkArgument(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
	}

	/**
	 * Permet de tester le caractère renseigné (non vide) d'une chaine.
	 * @param str String Chaine non vide
	 */
	public static void checkArgNotEmpty(final String str) {
		checkNotNull(str);
		if (StringUtil.isEmpty(str)) {
			throw new IllegalArgumentException("String must not be empty");
		}
	}

	/**
	 * Permet de tester le caractère renseigné (non vide) d'une chaine.
	 * @param str String Chaine non vide
	 * @param msg Message d'erreur
	 * @param params paramètres du message
	 */
	public static void checkArgNotEmpty(final String str, final String msg, final Object... params) {
		checkNotNull(str, msg, params);
		if (StringUtil.isEmpty(str)) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
	}

	//-------------------------------------------------------------------------
	//-------------------------IllegalStateException----------------------------
	//-------------------------------------------------------------------------
	/** 
	 * Vérification d'un état.
	 * S'utilise de maniére courante dans les calculs pour vérifer les états de variables au cours du traitement.
	 * S'utilise comme postCondition
	 * 
	 * @param test Expression booléenne qui doit être vérifiée
	 * @param msg Message affiché si le test <b>n'est pas</b> vérifié.
	 * @param params paramètres du message
	 */
	public static void checkState(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalStateException(StringUtil.format(msg, params));
		}
	}
}
