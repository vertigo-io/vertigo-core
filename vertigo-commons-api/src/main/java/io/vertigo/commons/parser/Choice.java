package io.vertigo.commons.parser;

/**
 * @author pchretien
 */
public final class Choice {
	private final int value;
	private final Object result;

	Choice(final int value, final Object result) {
		this.value = value;
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public int getValue() {
		return value;
	}
}
