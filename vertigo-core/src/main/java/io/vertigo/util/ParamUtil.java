package io.vertigo.util;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

public final class ParamUtil {
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	private ParamUtil() {
		//stateless
	}

	public static <O> O parse(final String paramName, final Class<O> paramType, final String paramValue) {
		return (O) doParse(paramName, paramType, paramValue);
	}

	private static Object doParse(final String paramName, final Class<?> paramType, final String paramValue) {
		Assertion.checkNotNull(paramName);
		Assertion.checkNotNull(paramType);
		Assertion.checkNotNull(paramValue);
		//-----
		try {
			if (String.class.equals(paramType)) {
				return paramValue;
			} else if (Boolean.class.equals(paramType) || boolean.class.equals(paramType)) {
				return toBoolean(paramName, paramValue);
			} else if (Integer.class.equals(paramType)) {
				return Integer.valueOf(paramValue);
			} else if (int.class.equals(paramType)) {
				return Integer.parseInt(paramValue);
			} else if (Long.class.equals(paramType)) {
				return Long.valueOf(paramValue);
			} else if (long.class.equals(paramType)) {
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
