/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

import java.io.Serializable;

import io.vertigo.core.locale.LocaleMessageKey;
import io.vertigo.core.locale.LocaleMessageText;

/**
 * Base exception for user-facing errors in Vertigo.
 * Features:
 * - Localized message support
 * - Parameter substitution
 * - Message key resolution
 * - Default message fallback
 * 
 * Used for errors that should be displayed to end users.
 *
 * @author fconstantin, pchretien
 */
public class VUserException extends RuntimeException {
	private static final long serialVersionUID = 33066445931128456L;
	private final LocaleMessageText messageText;

	/**
	 * Constructor.
	 * @param messageText Exception message
	 */
	public VUserException(final LocaleMessageText messageText) {
		// Use a method that doesn't throw exceptions
		super(messageText.getDisplay());
		// Re-enter the Exception API by passing the message
		this.messageText = messageText;
	}

	/**
	 * Constructor.
	 * @param defaultMsg The default msg (required)
	 * @param params List of params (optional)
	 */
	public VUserException(final String defaultMsg, final Serializable... params) {
		this(LocaleMessageText.of(defaultMsg, params));
	}

	/**
	 * Constructor.
	 * @param key The msg key (required)
	 * @param params List of params (optional)
	 */
	public VUserException(final LocaleMessageKey key, final Serializable... params) {
		this(LocaleMessageText.of(key, params));
	}

	/**
	 * Returns the externalized error message.
	 * @return The localized message text
	 */
	public final LocaleMessageText getMessageText() {
		return messageText;
	}
}
