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

import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.util.StringUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;


/**
 * Texte pouvant �tre externalis� dans un fichier de ressources,
 * en fonction du param�trage de l'application.
 * Si le libelle n'est pas trouv�, l'affichage est 
 * @author npiedeloup, pchretien
 */
public final class MessageText implements Serializable {
	private static final long serialVersionUID = 1L;
	/**Cl� du libell� dans le dictionnaire. */
	private final MessageKey key;
	/**Libell� non formatt�. */
	private final String defaultMsg;
	/**Param�tres permettant de formatter le libell�. */
	private final Serializable[] params;

	/**
	 * Constructeur.
	 * 
	 * @param key Cl� de la ressource
	 * @param params Param�tres de la ressource
	 */
	public MessageText(final MessageKey key, final Serializable... params) {
		this(null, key, params);
	}

	/**
	 * Constructeur.
	 * La cl� et/ou le message par d�faut doit �tre non null.
	 * 
	 * @param defaultMsg Message par d�faut (non formatt�) de la ressource
	 * @param key Cl� de la ressource
	 * @param params Param�tres de la ressource
	 */
	public MessageText(final String defaultMsg, final MessageKey key, final Serializable... params) {
		Assertion.checkArgument(!StringUtil.isEmpty(defaultMsg) || key != null, "La cl� ou le message dot �tre renseign�");
		//params n'est null que si l'on passe explicitement null
		//dans ce cas on le transforme en en tableau vide.
		// ----------------------------------------------------------------------
		this.key = key;
		this.defaultMsg = defaultMsg;
		this.params = params != null ? params : new Serializable[0];

	}

	/**
	 * @return Param�tres du message
	 */
	private Object[] getParams() {
		return params;
	}

	/**
	 * Formatte un message avec des param�tres.
	 * Ne lance aucune exception !!
	 * @return Message formatt�.
	 */
	public String getDisplay() {
		/*
		 * Cette m�thode doit toujours remonter un message. 
		 * Si LocaleManager n'est pas enregistr� ou g�n�re une exception 
		 * alors on se contente de retourner la cl� du message.
		 */
		Locale locale = null;
		String msg = null;
		if (key != null) {
			//On ne recherche le dictionnaire (g�r� par localeManager) que si il y a une cl�.
			final LocaleManager localeManager;
			try {
				//Il est n�cessaire que LocaleManager soit enregistr�.
				//Si pas d'utilisateur on prend la premi�re langue d�clar�e.
				localeManager = Home.getComponentSpace().resolve(LocaleManager.class);
				locale = localeManager.getCurrentLocale();
				msg = localeManager.getMessage(key, locale);
			} catch (final Throwable t) {
				//Si pas de locale msg est null et on va r�cup�rer s'il existe le message par d�faut.
			}
		}

		//Si pas de cl� on recherche le libell� par d�faut.
		if (msg == null) {
			msg = defaultMsg;
		}
		if (msg != null) {
			//On passe toujours dans le StringUtil.format pour unifier.
			return StringUtil.format(msg, getParams());
		}
		//On a rien trouv� on renvoit ce que l'on peut. (locale peut �tre null)
		return getPanicMessage(locale);
	}

	private String getPanicMessage(final Locale locale) {
		return new StringBuilder()//
				.append("<<")//
				.append(locale != null ? locale.getLanguage() : "xx")//
				.append(":")//
				.append(defaultMsg != null ? defaultMsg : key.name() + (params.length == 0 ? "" : Arrays.toString(params)))//
				.append(">>").toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getPanicMessage(null);
	}
}
