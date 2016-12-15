/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.component.loader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
import io.vertigo.util.ClassUtil;

/**
 * This container contains params initialized with String.
 * Method getUnusedKeys allows to identify phantom params.
 *
 * @author pchretien
 */
final class ComponentParamsContainer implements Container {
	private final Optional<ParamManager> paramManagerOption;
	private final Map<String, String> params;
	private final Set<String> unusedKeys;

	ComponentParamsContainer(final Optional<ParamManager> paramManagerOption, final Map<String, String> params) {
		Assertion.checkNotNull(paramManagerOption);
		Assertion.checkNotNull(params);
		//-----
		this.paramManagerOption = paramManagerOption;
		this.params = params;
		unusedKeys = new HashSet<>(params.keySet());
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.checkNotNull(id);
		//-----
		return params.containsKey(id);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O resolve(final String id, final Class<O> clazz) {
		Assertion.checkNotNull(id);
		Assertion.checkState(params.containsKey(id), "Le paramètre '{0}' de type '{1}' n'a pas été défini.", id, clazz.getSimpleName());
		//-----
		unusedKeys.remove(id);
		final Object value = getParamValue(id, clazz);
		final Class<O> type = ClassUtil.box(clazz);
		Assertion.checkArgument(type.isAssignableFrom(value.getClass()), "Composant/paramètre '{0}' type '{1}' , type attendu '{2}'", id, value.getClass(), clazz);
		return type.cast(value);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return Collections.unmodifiableSet(params.keySet());
	}

	/**
	 * Récupération d'un paramètre typé par son nom.
	 * @param paramName Nom du paramètre
	 * @param paramType Type du paramètre attendu
	 * @return Valeur sous forme texte du paramètre
	 */
	private Object getParamValue(final String paramName, final Class<?> paramType) {
		Assertion.checkNotNull(paramName);
		Assertion.checkNotNull(paramType);
		//-----
		final String paramValue = params.get(paramName);
		if (paramValue != null && paramValue.startsWith("${") && paramValue.endsWith("}")) {
			final String property = paramValue.substring("${".length(), paramValue.length() - "}".length());
			return paramManagerOption.orElseThrow(() -> new IllegalArgumentException("config is not allowed here"))
					.getValue(property, paramType);
		}
		return cast(paramName, ClassUtil.box(paramType), paramValue);
	}

	private static Object cast(final String paramName, final Class<?> paramType, final String value) {
		Assertion.checkArgument(!paramType.isPrimitive(), "only non primitive types are accepted for param " + paramName + " of type " + paramType);
		//-----
		if (String.class.equals(paramType)) {
			return value;
		} else if (Boolean.class.equals(paramType) || boolean.class.equals(paramType)) {
			return Boolean.valueOf(value);
		} else if (Integer.class.equals(paramType) || int.class.equals(paramType)) {
			return Integer.valueOf(value);
		} else if (Long.class.equals(paramType) || long.class.equals(paramType)) {
			return Long.valueOf(value);
		}
		throw new IllegalArgumentException("type '" + paramType + "' unsupported");
	}

	/*
	 * @return Keys that are not used, allows to identify phantom keys during injection.
	 */
	Set<String> getUnusedKeys() {
		return unusedKeys;
	}
}
