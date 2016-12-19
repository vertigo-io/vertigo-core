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
import io.vertigo.util.ParamUtil;

/**
 * This container contains params initialized with String.
 * Method getUnusedKeys allows to identify 'phantom' params.
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
		Assertion.checkState(params.containsKey(id), "param '{0}' of type '{1}' has not be registered.", id, clazz.getSimpleName());
		//-----
		unusedKeys.remove(id);
		return getParamValue(id, clazz);
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
	private <O> O getParamValue(final String paramName, final Class<O> paramType) {
		Assertion.checkNotNull(paramName);
		Assertion.checkNotNull(paramType);
		//-----
		final String paramValue = params.get(paramName);
		if (paramValue != null && paramValue.startsWith("${") && paramValue.endsWith("}")) {
			final String property = paramValue.substring("${".length(), paramValue.length() - "}".length());
			return paramManagerOption.orElseThrow(() -> new IllegalArgumentException("config is not allowed here"))
					.getValue(property, paramType);
		}
		return ParamUtil.parse(paramName, paramType, paramValue);
	}

	/*
	 * @return Keys that are not used, allows to identify phantom keys during injection.
	 */
	Set<String> getUnusedKeys() {
		return unusedKeys;
	}
}
