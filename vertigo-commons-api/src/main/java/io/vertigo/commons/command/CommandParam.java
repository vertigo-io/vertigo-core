package io.vertigo.commons.command;

import java.lang.reflect.Type;

import io.vertigo.lang.Assertion;

public final class CommandParam {

	private final Type paramType;

	public CommandParam(final Type paramType) {
		Assertion.checkNotNull(paramType);
		//---
		this.paramType = paramType;
	}

	public Type getType() {
		return paramType;
	}

}
