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
package io.vertigo.core.locale;

import java.io.Serializable;

import io.vertigo.lang.Builder;

/**
 * Builder to create a complex MessageText.
 * @author pchretien
 */
public final class MessageTextBuilder implements Builder<MessageText> {
	private static Serializable[] EMPTY = new Serializable[0];
	private MessageKey myKey;
	private String myDefaultMsg;
	private Serializable[] myParams = EMPTY;

	MessageTextBuilder() {
		super();
	}

	/**
	 * Message text with a key
	 * @param key the key to use
	 * @return this builder
	 */
	public MessageTextBuilder withKey(final MessageKey key) {
		myKey = key;
		return this;
	}

	/**
	 * Message text with a defaultMessage
	 * @param defaultMsg the defaultMessage
	 * @return this builder
	 */
	public MessageTextBuilder withDefaultMsg(final String defaultMsg) {
		myDefaultMsg = defaultMsg;
		return this;
	}

	/**
	 * Message text with params
	 * @param params the params
	 * @return this builder
	 */
	public MessageTextBuilder withParams(final Serializable... params) {
		myParams = params;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public MessageText build() {
		return new MessageText(myDefaultMsg, myKey, myParams);
	}

}
