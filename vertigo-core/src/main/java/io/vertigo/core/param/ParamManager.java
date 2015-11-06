/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.param;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Interface du gestionnaire de la configuration applicative.
 * 
 * Une configuration possède une liste de paramètres.
 * Un paramètre est 
 *  - identifié par un nom.
 *  - camelCase.camelCase et ne contient que des lettres et chiffres; les séparateurs sont des points.
 * 	
 * Les paramètres sont de trois types :
 * -boolean
 * -String
 * -int
 *
 *
 * Exemple en json :
 *
 * {
 *  server.host : "wiki",
 *  server.port : "5455",
 *  maxUsers  :"10",
 * }
 *
 *
 * getStringValue("server.host") => wiki
 * getStringValue("host") => erreur.
 *
 * @author pchretien, npiedeloup, prahmoune
 */
public final class ParamManager implements Component {
	/** Regexp paramName. */
	private static final Pattern REGEX_PARAM_NAME = Pattern.compile("([a-zA-Z]+)([\\._-][a-zA-Z0-9]+)*");
	private final List<ParamPlugin> paramPlugins;
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	@Inject
	public ParamManager(final List<ParamPlugin> paramPlugins) {
		Assertion.checkNotNull(paramPlugins);
		//-----
		this.paramPlugins = paramPlugins;
	}

	private static void checkParamName(final String paramName) {
		Assertion.checkArgNotEmpty(paramName);
		Assertion.checkArgument(REGEX_PARAM_NAME.matcher(paramName).matches(), "param '{0}' must match pattern {1}", paramName, REGEX_PARAM_NAME);
	}

	public <C> C getValue(final String paramName, final Class<C> paramType) {
		checkParamName(paramName);
		Assertion.checkNotNull(paramType);
		//-----
		final String paramValue = doGetParamValueAsString(paramName);
		return castValue(paramName, paramType, paramValue);
	}

	/**
	 * Return a param as a String.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	public String getStringValue(final String paramName) {
		return getValue(paramName, String.class);
	}

	/**
	 * Return a param as an int.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	public Integer getIntValue(final String paramName) {
		return getValue(paramName, Integer.class);
	}

	/**
	 * Return a param as a long.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	public Long getLongValue(final String paramName) {
		return getValue(paramName, Long.class);
	}

	/**
	 * Return a param as a boolean .
	 * @param paramName param's name
	 * @return Value of the param
	 */
	public Boolean getBooleanValue(final String paramName) {
		return getValue(paramName, Boolean.class);
	}

	/**
	 * @param paramName param's name
	 * @return Value of the param
	 */
	private String doGetParamValueAsString(final String paramName) {
		checkParamName(paramName);
		//-----
		for (final ParamPlugin paramPlugin : paramPlugins) {
			final Option<String> value = paramPlugin.getValue(paramName);
			if (value.isDefined()) {
				return value.get();
			}
		}
		throw new IllegalArgumentException("param '" + paramName + "' not found");
	}

	/**
	 * Cast the paramValue into the paramClass.
	 * @param paramName param's name
	 * @param paramClass param's class
	 * @return casted value
	 */
	private static <C> C castValue(final String paramName, final Class<C> paramClass, final String paramValue) {
		final Object value;
		if (boolean.class.equals(paramClass) || Boolean.class.equals(paramClass)) {
			value = toBoolean(paramName, paramValue);
		} else if (long.class.equals(paramClass) || Long.class.equals(paramClass)) {
			value = toLong(paramName, paramValue);
		} else if (int.class.equals(paramClass) || Integer.class.equals(paramClass)) {
			value = toInteger(paramName, paramValue);
		} else if (String.class.equals(paramClass)) {
			value = paramValue;
		} else {
			throw new IllegalArgumentException("Param :" + paramName + " of type ' " + paramClass + " is not supported");
		}
		return paramClass.cast(value);
	}

	private static Boolean toBoolean(final String paramName, final String paramValue) {
		if (!(TRUE.equalsIgnoreCase(paramValue) || FALSE.equalsIgnoreCase(paramValue))) {
			throw new RuntimeException("Param :" + paramName + " with value ' " + paramValue + " can't be cast into 'boolean'");
		}
		return Boolean.parseBoolean(paramValue);
	}

	private static Integer toInteger(final String paramName, final String paramValue) {
		try {
			return Integer.parseInt(paramValue);
		} catch (final NumberFormatException e) {
			throw new RuntimeException("Param :" + paramName + " with value ' " + paramValue + " can't be cast into 'int'");
		}
	}

	private static Long toLong(final String paramName, final String paramValue) {
		try {
			return Long.parseLong(paramValue);
		} catch (final NumberFormatException e) {
			throw new RuntimeException("Param :" + paramName + " with value ' " + paramValue + " can't be cast into 'long'");
		}
	}
}
