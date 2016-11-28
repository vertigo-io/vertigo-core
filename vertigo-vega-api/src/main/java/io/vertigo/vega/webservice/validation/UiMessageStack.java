/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.vega.webservice.validation;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Class d'enregistrement des messages.
 * @author npiedeloup
 */
public interface UiMessageStack {

	/**
	 * Niveau du message.
	 * @author npiedeloup
	 */
	public enum Level {
		/** Erreur. */
		ERROR,
		/** Warning. */
		WARNING,
		/** Info. */
		INFO,
		/** Success. */
		SUCCESS;
	}

	/**
	 * Ajoute un message.
	 * @param level Niveau de message
	 * @param message Message
	 */
	void addGlobalMessage(final Level level, final String message);

	/**
	 * @param message Message d'erreur
	 */
	void error(final String message);

	/**
	 * @param message Message d'alerte
	 */
	void warning(final String message);

	/**
	 * @param message Message d'info
	 */
	void info(final String message);

	/**
	 * @param message Message d'info
	 */
	void success(final String message);

	/**
	 * @param message Message d'erreur
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	void error(final String message, final DtObject dto, final String fieldName);

	/**
	 * @param message Message d'alerte
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	void warning(final String message, final DtObject dto, final String fieldName);

	/**
	 * @param message Message d'info
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	void info(final String message, final DtObject dto, final String fieldName);

	void addFieldMessage(Level level, String message, DtObject dtObject, String fieldName);

	void addFieldMessage(Level level, String message, String contextKey, String fieldName);

	/**
	 * @return if there are errors in this stack.
	 */
	public boolean hasErrors();

}
