package io.vertigo.lang;

import java.io.Serializable;

/**
 *
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

	public MessageTextBuilder withKey(final MessageKey key) {
		this.myKey = key;
		return this;
	}

	public MessageTextBuilder withDefaultMsg(final String defaultMsg) {
		this.myDefaultMsg = defaultMsg;
		return this;
	}

	public MessageTextBuilder withParams(final Serializable... params) {
		this.myParams = params;
		return this;
	}

	@Override
	public MessageText build() {
		return new MessageText(myDefaultMsg, myKey, myParams);
	}

}
