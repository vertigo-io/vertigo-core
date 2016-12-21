package io.vertigo.core.param;

import java.util.regex.Pattern;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * Param.
 * @author pchretien
 */
public final class Param {
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	/** Regexp paramName. */
	private static final Pattern REGEX_PARAM_NAME = Pattern.compile("([a-zA-Z]+)([\\._-][a-zA-Z0-9]+)*");
	private final String name;
	private final String value;

	/**
	 * Constructor
	 * @param name the name of the param
	 * @param value the value of the param
	 */
	private Param(final String name, final String value) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgument(REGEX_PARAM_NAME.matcher(name).matches(), "param '{0}' must match pattern {1}", name, REGEX_PARAM_NAME);
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

	/**
	 * Returns the param ad a typed value .
	 * @param paramType Type of the param
	 * @return the value of the param
	 */
	public <O> O getValue(final Class<O> paramType) {
		Assertion.checkNotNull(paramType);
		//-----
		return (O) parse(name, paramType, value);
	}

	/**
	 * Returns the param as a String.
	 * @return the value of the param
	 */
	public String getValueAsString() {
		return getValue(String.class);
	}

	/**
	 * Returns the param as an int.
	 * @return the value of the param
	 */
	public int getValueAsInt() {
		return getValue(int.class);
	}

	/**
	 * Returns the param as a long.
	 * @return the value of the param
	 */
	public long getValueAsLong() {
		return getValue(long.class);
	}

	/**
	 * Returns the param as a boolean .
	 * @return the value of the param
	 */
	public boolean getValueAsBoolean() {
		return getValue(boolean.class);
	}

	private static Object parse(final String paramName, final Class<?> paramType, final String paramValue) {
		Assertion.checkNotNull(paramName);
		Assertion.checkNotNull(paramType);
		Assertion.checkNotNull(paramValue);
		//-----
		try {
			if (String.class.equals(paramType)) {
				return paramValue;
			} else if (Boolean.class.equals(paramType) || boolean.class.equals(paramType)) {
				return toBoolean(paramName, paramValue);
			} else if (Integer.class.equals(paramType) || int.class.equals(paramType)) {
				return Integer.parseInt(paramValue);
			} else if (Long.class.equals(paramType) || long.class.equals(paramType)) {
				return Long.parseLong(paramValue);
			} else {
				throw new IllegalArgumentException("type '" + paramType + "' unsupported");
			}
		} catch (final Exception e) {
			throw new VSystemException(e, "Param :{0} with value :{1} can't be cast into '{2}'", paramName, paramValue, paramType);
		}
	}

	private static boolean toBoolean(final String paramName, final String paramValue) {
		if (!(TRUE.equalsIgnoreCase(paramValue) || FALSE.equalsIgnoreCase(paramValue))) {
			throw new VSystemException("Param :{0} with value :{1} can't be cast into 'boolean'", paramName, paramValue);
		}
		return Boolean.parseBoolean(paramValue);
	}
}
