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
 * Permet de g�rer les Assertions. 
 * C'est � dire les : 
 * <ul>
 * 	<li>pr� conditions : validation des arguments</li>
 * 	<li>invariants / post conditions : validation des �tats</li>
 * </ul>
 * Ces notions ont �t� introduites avec le langage Eiffel par B. Meyer. Elles sont relatives � la notion de contrat.
 * Il s'agit de v�rifier
 * les contrats en entr�e <b>-pr�condition-</b>
 * les contrats en sortie <b>-postcondition-</b>
 * les conditions obligatoirement v�rifi�es � l'int�rieur des m�thodes <b>-invariant-</b>.
 * <br>Pour aller plus loin lire articles et ouvrages autour des travaux de B. Meyer et M. Fowler concernant la programmation par contrat (design by contract).
 *
 * @author fconstantin
 * @version $Id: Assertion.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public final class Assertion {
	//-------------------------------------------------------------------------
	//-------------------------NullPointerException----------------------------
	//-------------------------------------------------------------------------

	/**
	 * Permet de tester le caract�re obligatoire (non null) d'un objet.
	 * @param o Object Objet obligatoire
	 */
	public static void checkNotNull(final Object o) {
		if (o == null) {
			// Optimis� pour message sans formattage
			throw new NullPointerException();
		}
	}

	/**
	 * Permet de tester le caract�re obligatoire (non null) d'un objet.
	 * @param o Object Objet obligatoire
	 * @param msg Message d'erreur
	 * @param params Param�tres du message
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
	 * Utilis� comme validation des pr�conditions.
	 * @param test Expression bool�enne qui doit �tre v�rifi�e
	 * @param msg Message affich� si le test <b>n'est pas</b> v�rifi�.
	 * @param params Param�tres du message
	 */
	public static void checkArgument(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalArgumentException(StringUtil.format(msg, params));
		}
	}

	/**
	 * Permet de tester le caract�re renseign� (non vide) d'une chaine.
	 * @param str String Chaine non vide
	 */
	public static void checkArgNotEmpty(final String str) {
		checkNotNull(str);
		if (StringUtil.isEmpty(str)) {
			throw new IllegalArgumentException("String must not be empty");
		}
	}

	/**
	 * Permet de tester le caract�re renseign� (non vide) d'une chaine.
	 * @param str String Chaine non vide
	 * @param msg Message d'erreur
	 * @param params Param�tres du message
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
	 * V�rification d'un �tat.
	 * S'utilise de mani�re courante dans les calculs pour v�rifer les �tats de variables au cours du traitement.
	 * S'utilise comme postCondition
	 * 
	 * @param test Expression bool�enne qui doit �tre v�rifi�e
	 * @param msg Message affich� si le test <b>n'est pas</b> v�rifi�.
	 * @param params Param�tres du message
	 */
	public static void checkState(final boolean test, final String msg, final Object... params) {
		if (!test) {
			throw new IllegalStateException(StringUtil.format(msg, params));
		}
	}
}
