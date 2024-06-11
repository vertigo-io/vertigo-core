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
 * A Text that can be externalized in a resource file,
 * based on the application's configuration settings.
 * If the label is not found, the display is
 *
 * @author npiedeloup, pchretien
 */
public final class LocaleMessageText implements Serializable {
	private static final long serialVersionUID = 4723023230514051954L;
	/** Key of the label in the dictionary. */
	private final LocaleMessageKey key;
	/** Unformatted label. */
	private final String defaultMsg;
	/** Parameters allowing to format the label. */
	private final Serializable[] params;

	/**
	 * Constructor.
	 * The key and/or default message must be non-null.
	 *
	 * @param defaultMsg Default message (unformatted) of the resource
	 * @param key Key of the resource
	 * @param params Parameters of the resource
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
	 * Static Builder of a messageText by its key.
	 *
	 * @param key Key of the resource
	 * @return the messageText
	 */
	public static LocaleMessageText of(final LocaleMessageKey key, final Serializable... params) {
		Assertion.check().isNotNull(key, "the message key is required");
		//---
		return new LocaleMessageText(null, key, params);
	}

	/**
	 * Static Builder of a messageText by its default message.
	 *
	 * @param msg Default message (unformatted) of the resource
	 * @return the messageText
	 */
	public static LocaleMessageText of(final String msg, final Serializable... params) {
		Assertion.check().isNotBlank(msg, "the message is required");
		//---
		return new LocaleMessageText(msg, null, params);
	}

	/**
	 * Static Builder of a messageText by its default message.
	 *
	 * @param defaultMsg Default message (unformatted) of the resource
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
	 * @return parameters of the message
	 */
	private Object[] getParams() {
		return params;
	}

	/**
	 * Format message with parameters.
	 * No exception thrown!!
	 *
	 * @return Formatted message, if exists.
	 */
	public Optional<String> getDisplayOpt() {
		Locale locale = null;
		String msg = null;
		if (key != null) {
			// Only search the dictionary (managed by localeManager) if there is a key.
			try {
				// It is necessary that LocaleManager is registered.
				// If no user is present, the first declared language is taken.
				final var localeManager = getLocaleManager();
				locale = localeManager.getCurrentLocale();
				msg = localeManager.getMessage(key, locale);
			} catch (final Exception e) {
				// If LocaleManager is not registered or generates an exception
				// then msg remains null and we retrieve the default message if it exists.
			}
		}

		// If no key is found, we search for the default message.
		if (msg == null) {
			msg = defaultMsg;
		}
		if (msg != null) {
			// We always pass through StringUtil.format to unify.
			return Optional.of(StringUtil.format(msg, getParams()));
		}

		return Optional.empty();
	}

	/**
	 * Format message with parameters.
	 * If nothing is found, return a "panic message" displaying what is missing.
	 * No exception thrown!!
	 *
	 * @return Formatted message.
	 */
	public String getDisplay() {
		/*
		 * This method must always return a message.
		 * If LocaleManager is not registered or generates an exception
		 * then we return the key of the message.
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
