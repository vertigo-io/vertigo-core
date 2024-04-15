/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.locale;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.util.StringUtil;

/**
 * Texte pouvant être externalisé dans un fichier de ressources,
 * en fonction du paramétrage de l'application.
 * Si le libelle n'est pas trouvé, l'affichage est
 *
 * @author npiedeloup, pchretien
 */
public final class LocaleMessageText implements Serializable {
	private static final long serialVersionUID = 4723023230514051954L;
	/** Clé du libellé dans le dictionnaire. */
	private final LocaleMessageKey key;
	/** Libellé non formatté. */
	private final String defaultMsg;
	/** paramètres permettant de formatter le libellé. */
	private final Serializable[] params;

	/**
	 * Constructor.
	 * La clé et/ou le message par défaut doit être non null.
	 *
	 * @param defaultMsg Message par défaut (non formatté) de la ressource
	 * @param key Clé de la ressource
	 * @param params paramètres de la ressource
	 */
	private LocaleMessageText(final String defaultMsg, final LocaleMessageKey key, final Serializable... params) {
		Assertion.check()
				.isNotNull(params)
				.isTrue(defaultMsg != null || key != null, "key or msg must be defined");
		//---
		this.key = key;
		this.defaultMsg = defaultMsg;
		this.params = params;
	}

	/**
	 * static Builder of a messageText by its key.
	 *
	 * @param key Clé de la ressource
	 * @return the messageText
	 */
	public static LocaleMessageText of(final LocaleMessageKey key, final Serializable... params) {
		Assertion.check().isNotNull(key, "the message key is required");
		//---
		return new LocaleMessageText(null, key, params);
	}

	/**
	 * static Builder of a messageText by its default message.
	 *
	 * @param msg Message par défaut (non formatté) de la ressource
	 * @return the messageText
	 */
	public static LocaleMessageText of(final String msg, final Serializable... params) {
		Assertion.check().isNotBlank(msg, "the message is required");
		//---
		return new LocaleMessageText(msg, null, params);
	}

	/**
	 * static Builder of a messageText by its default message.
	 *
	 * @param defaultMsg Message par défaut (non formatté) de la ressource
	 * @return the messageText
	 */
	public static LocaleMessageText ofDefaultMsg(final String defaultMsg, final LocaleMessageKey key, final Serializable... params) {
		Assertion.check()
				.isNotBlank(defaultMsg, "the default message is required")
				.isNotNull(key, "the message key is required");
		//---
		return new LocaleMessageText(defaultMsg, key, params);
	}

	/**
	 * @return paramètres du message
	 */
	private Object[] getParams() {
		return params;
	}

	/**
	 * Format message with parameters.
	 * No exception throwed !!
	 *
	 * @return Formatted message, if exists.
	 */
	public Optional<String> getDisplayOpt() {
		Locale locale = null;
		String msg = null;
		if (key != null) {
			//On ne recherche le dictionnaire (géré par localeManager) que si il y a une clé.
			try {
				//Il est nécessaire que LocaleManager soit enregistré.
				//Si pas d'utilisateur on prend la première langue déclarée.
				final var localeManager = getLocaleManager();
				locale = localeManager.getCurrentLocale();
				msg = localeManager.getMessage(key, locale);
			} catch (final Exception e) {
				//Si pas de locale msg est null et on va récupérer s'il existe le message par défaut.
			}
		}

		//Si pas de clé on recherche le libellé par défaut.
		if (msg == null) {
			msg = defaultMsg;
		}
		if (msg != null) {
			//On passe toujours dans le StringUtil.format pour unifier.
			return Optional.of(StringUtil.format(msg, getParams()));
		}

		return Optional.empty();
	}

	/**
	 * Format message with parameters.
	 * If nothing found, return a "panic message" displaying what is missing.
	 * No exception throwed !!
	 *
	 * @return Formatted message.
	 */
	public String getDisplay() {
		/*
		 * Cette méthode doit toujours remonter un message.
		 * Si LocaleManager n'est pas enregistré ou génère une exception
		 * alors on se contente de retourner la clé du message.
		 */
		return getDisplayOpt().orElseGet(() -> getPanicMessage());
	}

	private String getPanicMessage() {
		Locale locale = null;
		try {
			locale = getLocaleManager().getCurrentLocale();
		} catch (final Exception e) {
			// nothing
		}
		return new StringBuilder()
				.append("<<")
				.append(locale != null ? locale.getLanguage() : "xx")
				.append(":")
				.append(defaultMsg != null ? defaultMsg : key.name() + (params.length == 0 ? "" : Arrays.toString(params)))
				.append(">>")
				.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getPanicMessage();
	}

	private static LocaleManager getLocaleManager() {
		return Node.getNode().getComponentSpace().resolve(LocaleManager.class);
	}
}
