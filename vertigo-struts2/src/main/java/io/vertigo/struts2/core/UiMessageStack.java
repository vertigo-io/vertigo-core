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
package io.vertigo.struts2.core;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Class d'enregistrement des messages.
 *
 * @author npiedeloup
 */
public final class UiMessageStack {
	private final AbstractActionSupport actionSupport;

	private static final String FIELD_LABEL_PREFIX = "<label>";
	private static final String FIELD_LABEL_SUFFIX = "</label>";

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
		INFO;

		private final String prefixMarker;

		private Level() {
			prefixMarker = toString() + ":";
		}

		/** @return Prefix du niveau de message.*/
		String getPrefixMarker() {
			return prefixMarker;
		}
	}

	/**
	 * Constructeur.
	 * @param actionSupport Action où déverser les messages
	 */
	protected UiMessageStack(final AbstractActionSupport actionSupport) {
		Assertion.checkNotNull(actionSupport);
		//-----
		this.actionSupport = actionSupport;
	}

	/**
	 * Ajoute un message.
	 * @param level Niveau de message
	 * @param message Message
	 */
	public void addActionMessage(final Level level, final String message) {
		if (level == Level.ERROR) {
			actionSupport.addActionError(message);
		} else {
			actionSupport.addActionMessage(level.getPrefixMarker() + message);
		}
	}

	/**
	 * @param message Message d'erreur
	 */
	public void error(final String message) {
		addActionMessage(Level.ERROR, message);
	}

	/**
	 * @param message Message d'alerte
	 */
	public void warning(final String message) {
		addActionMessage(Level.WARNING, message);
	}

	/**
	 * @param message Message d'info
	 */
	public void info(final String message) {
		addActionMessage(Level.INFO, message);
	}

	/**
	 * Ajoute un message.
	 * @param level Niveau de message
	 * @param message Message
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champs portant l'erreur
	 */
	public void addActionMessage(final Level level, final String message, final DtObject dto, final String fieldName) {
		Assertion.checkNotNull(level);
		Assertion.checkArgNotEmpty(message);
		Assertion.checkNotNull(dto);
		Assertion.checkArgNotEmpty(fieldName);
		//-----
		final String contextKey = actionSupport.getModel().findKey(dto);
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		addActionMessage(level, message, dtDefinition, contextKey, fieldName);
	}

	/**
	 * @param message Message d'erreur
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public void error(final String message, final DtObject dto, final String fieldName) {
		addActionMessage(Level.ERROR, message, dto, fieldName);
	}

	/**
	 * @param message Message d'alerte
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public void warning(final String message, final DtObject dto, final String fieldName) {
		addActionMessage(Level.WARNING, message, dto, fieldName);
	}

	/**
	 * @param message Message d'info
	 * @param dto Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public void info(final String message, final DtObject dto, final String fieldName) {
		addActionMessage(Level.INFO, message, dto, fieldName);
	}

	/**
	 * Ajoute un message.
	 * @param level Niveau de message
	 * @param message Message
	 * @param uiObject Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public void addActionMessage(final Level level, final String message, final UiObject<?> uiObject, final String fieldName) {
		addActionMessage(level, message, uiObject.getInnerObject(), fieldName);
	}

	/**
	 * @param message Message d'erreur
	 * @param uiObject Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public void error(final String message, final UiObject<?> uiObject, final String fieldName) {
		addActionMessage(Level.ERROR, message, uiObject, fieldName);
	}

	/**
	 * @param message Message d'alerte
	 * @param uiObject Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public void warning(final String message, final UiObject<?> uiObject, final String fieldName) {
		addActionMessage(Level.WARNING, message, uiObject, fieldName);
	}

	/**
	 * @param message Message d'info
	 * @param uiObject Objet portant les erreurs
	 * @param fieldName Champ portant l'erreur
	 */
	public void info(final String message, final UiObject<?> uiObject, final String fieldName) {
		addActionMessage(Level.INFO, message, uiObject, fieldName);
	}

	private void addActionMessage(final Level level, final String message, final DtDefinition dtDefinition, final String contextKey, final String fieldName) {
		if (level == Level.ERROR) {
			actionSupport.addFieldError(contextKey + "." + fieldName, message);
		} else {
			final String constFieldName = StringUtil.camelToConstCase(fieldName);
			final DtField dtField = dtDefinition.getField(constFieldName);
			actionSupport.addActionMessage(level.getPrefixMarker() + FIELD_LABEL_PREFIX + dtField.getLabel().getDisplay() + FIELD_LABEL_SUFFIX + message);
		}
	}

	public boolean hasErrors() {
		return actionSupport.hasActionErrors() || actionSupport.hasErrors() || actionSupport.hasFieldErrors();
	}
}
