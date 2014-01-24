package io.vertigo.commonsimpl.util.parser;

/**
 * @author pchretien
 * @version $Id: Choice.java,v 1.1 2013/07/30 16:57:06 pchretien Exp $
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
