package io.vertigo.rest;

import io.vertigo.kernel.lang.Assertion;

/**
* @author npiedeloup
*/
public final class EndPointParam {
	private final String name;
	private final Class<?> type;

	public EndPointParam(final String name, final Class<?> type) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		//-----------------------------------------------------------------
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}
}
