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
		Assertion.checkNotNull(paramName);
		Assertion.checkNotNull(paramType);
		Assertion.checkNotNull(paramValue);
		//-----
		Object parsed;
		try {

			if (String.class.equals(paramType)) {
				parsed = paramValue;
			} else if (Boolean.class.equals(paramType)) {
				parsed = toBoolean(paramName, paramValue);
			} else if (boolean.class.equals(paramType)) {
				parsed = toBoolean(paramName, paramValue);
			} else if (Integer.class.equals(paramType)) {
				parsed = Integer.valueOf(paramValue);
			} else if (int.class.equals(paramType)) {
				parsed = Integer.parseInt(paramValue);
			} else if (Long.class.equals(paramType)) {
				parsed = Long.valueOf(paramValue);
			} else if (long.class.equals(paramType)) {
				parsed = Long.parseLong(paramValue);
			} else {
				throw new IllegalArgumentException("type '" + paramType + "' unsupported");
			}
		} catch (final Exception e) {
			throw new VSystemException(e, "Param :{0} with value :{1} can't be cast into '{2}'", paramName, paramValue, paramType);
		}

		return (O) parsed;
	}

	private static boolean toBoolean(final String paramName, final String paramValue) {
		if (!(TRUE.equalsIgnoreCase(paramValue) || FALSE.equalsIgnoreCase(paramValue))) {
			throw new VSystemException("Param :{0} with value :{1} can't be cast into 'boolean'", paramName, paramValue);
		}
		return Boolean.parseBoolean(paramValue);
	}
}
