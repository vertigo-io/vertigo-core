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
