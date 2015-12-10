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
import io.vertigo.lang.Option;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.ClassUtil;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Standard implementation of the paramManager.
 * 
 * The strategy to access params is defined in a list of plugins.
 * 
 * 
 * @author pchretien, npiedeloup, prahmoune
 */
public final class ParamManagerImpl implements ParamManager {
	/** Regexp paramName. */
	private static final Pattern REGEX_PARAM_NAME = Pattern.compile("([a-zA-Z]+)([\\._-][a-zA-Z0-9]+)*");
	private final List<ParamPlugin> paramPlugins;
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	/**
	 * Constructor.
	 * @param paramPlugins List of plugins
	 */
	@Inject
	public ParamManagerImpl(final List<ParamPlugin> paramPlugins) {
		Assertion.checkNotNull(paramPlugins);
		//-----
		this.paramPlugins = paramPlugins;
	}

	private static void checkParamName(final String paramName) {
		Assertion.checkArgNotEmpty(paramName);
		Assertion.checkArgument(REGEX_PARAM_NAME.matcher(paramName).matches(), "param '{0}' must match pattern {1}", paramName, REGEX_PARAM_NAME);
	}

	/** {@inheritDoc} */
	@Override
	public <C> C getValue(final String paramName, final Class<C> paramType) {
		checkParamName(paramName);
		Assertion.checkNotNull(paramType);
		//-----
		final String paramValue = doGetParamValueAsString(paramName);
		return (C) cast(paramName, ClassUtil.box(paramType), paramValue);
	}

	private static Object cast(final String paramName, final Class<?> paramType, final String paramValue) {
		Assertion.checkArgument(!paramType.isPrimitive(), "only non primitive types are accepted");
		//-----
		if (String.class.equals(paramType)) {
			return paramValue;
		} else if (Boolean.class.equals(paramType)) {
			return toBoolean(paramName, paramValue);
		} else if (Integer.class.equals(paramType)) {
			return toInteger(paramName, paramValue);
		} else if (Long.class.equals(paramType)) {
			return toLong(paramName, paramValue);
		}
		throw new IllegalArgumentException("type '" + paramType + "' unsupported");
	}

	/** {@inheritDoc} */
	@Override
	public String getStringValue(final String paramName) {
		return getValue(paramName, String.class);
	}

	/** {@inheritDoc} */
	@Override
	public int getIntValue(final String paramName) {
		return toInteger(paramName, doGetParamValueAsString(paramName));
	}

	/** {@inheritDoc} */
	@Override
	public long getLongValue(final String paramName) {
		return toLong(paramName, doGetParamValueAsString(paramName));
	}

	/** {@inheritDoc} */
	@Override
	public boolean getBooleanValue(final String paramName) {
		return toBoolean(paramName, doGetParamValueAsString(paramName));
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

	private static boolean toBoolean(final String paramName, final String paramValue) {
		if (!(TRUE.equalsIgnoreCase(paramValue) || FALSE.equalsIgnoreCase(paramValue))) {
			throw new VSystemException("Param :{0} with value :{1} can't be cast into 'boolean'", paramName, paramValue);
		}
		return Boolean.parseBoolean(paramValue);
	}

	private static Integer toInteger(final String paramName, final String paramValue) {
		try {
			return Integer.parseInt(paramValue);
		} catch (final NumberFormatException e) {
			throw new VSystemException("Param :{0} with value :{1} can't be cast into 'int'", paramName, paramValue);
		}
	}

	private static long toLong(final String paramName, final String paramValue) {
		try {
			return Long.parseLong(paramValue);
		} catch (final NumberFormatException e) {
			throw new VSystemException("Param :{0} with value :{1} can't be cast into 'long'", paramName, paramValue);
		}
	}
}
