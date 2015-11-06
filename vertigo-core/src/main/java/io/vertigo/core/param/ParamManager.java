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
	private static final Pattern REGEX_PARAM_NAME = Pattern.compile("([a-z][a-zA-Z0-9]*)(\\.[a-z][a-zA-Z0-9]*)*");
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
		Assertion.checkArgument(REGEX_PARAM_NAME.matcher(paramName).matches(), "param '{0}' must be camelCase and start with a lowercase", paramName);
	}

	/**
	 * Return a param as a String.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	public String getStringValue(final String paramName) {
		return doGetParamValue(paramName, String.class);
	}

	/**
	 * Return a param as an int.
	 * @param paramName param's name
	 * @return Value of the param
	 */
	public int getIntValue(final String paramName) {
		return doGetParamValue(paramName, int.class);
	}

	/**
	 * Return a param as a boolean .
	 * @param paramName param's name
	 * @return Value of the param
	 */
	public boolean getBooleanValue(final String paramName) {
		return doGetParamValue(paramName, boolean.class);
	}

	/**
	 * @param paramName param's name
	 * @param paramClass param's class
	 * @return Value of the param
	 */
	private <C> C doGetParamValue(final String paramName, final Class<C> paramClass) {
		checkParamName(paramName);
		//-----
		for (final ParamPlugin paramPlugin : paramPlugins) {
			final Option<String> value = paramPlugin.getValue(paramName);
			if (value.isDefined()) {
				return (C) castValue(paramName, paramClass, value.get());
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
	private static Object castValue(final String paramName, final Class<?> paramClass, final String paramValue) {
		if (boolean.class.equals(paramClass)) {
			return toBoolean(paramName, paramValue);
		} else if (int.class.equals(paramClass)) {
			return toInteger(paramName, paramValue);
		} else if (String.class.equals(paramClass)) {
			return paramValue;
		}
		throw new IllegalArgumentException("Param :" + paramName + " of type ' " + paramClass + " is not supported");
	}

	private static boolean toBoolean(final String paramName, final String paramValue) {
		if (!(TRUE.equalsIgnoreCase(paramValue) || FALSE.equalsIgnoreCase(paramValue))) {
			throw new RuntimeException("Param :" + paramName + " with value ' " + paramValue + " can't be cast into 'boolean'");
		}
		return Boolean.parseBoolean(paramValue);
	}

	private static int toInteger(final String paramName, final String paramValue) {
		try {
			return Integer.parseInt(paramValue);
		} catch (final NumberFormatException e) {
			throw new RuntimeException("Param :" + paramName + " with value ' " + paramValue + " can't be cast into 'int'");
		}
	}
}
