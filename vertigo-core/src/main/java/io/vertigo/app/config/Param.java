package io.vertigo.app.config;

import io.vertigo.lang.Assertion;

/**
 * Param of components (plugins).
 * @author pchretien
 */
public final class Param {
	private final String name;
	private final String value;

	/**
	 * Constructor
	 * @param name the name of the param
	 * @param value the value of the param
	 */
	private Param(final String name, final String value) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(value);
		//---
		this.name = name;
		this.value = value;
	}

	/***
	 * Creates a new param
	 * @param name the name of the param
	 * @param value the value of the param
	 * @return new param
	 */
	public static Param create(final String name, final String value) {
		return new Param(name, value);
	}

	/**
	 * @return the value of the param
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the name of the param
	 */
	public String getName() {
		return name;
	}
}
