/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.node.config;

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Amplifier;
import io.vertigo.core.param.Param;

/**
 * This class defines the configuration of an amplifier.
 *
 * A amplifier is defined by
 *  - an api interface 
 *  - a map of params
 *
 * @author pchretien
 */
public final class AmplifierConfig {
	private final Class<? extends Amplifier> apiClass;
	private final List<Param> params;

	/**
	 * Constructor.
	 * @param apiClass the api class of the amlifier 
	 * @param params the params
	 */
	AmplifierConfig(final Class<? extends Amplifier> apiClass, final List<Param> params) {
		Assertion.checkNotNull(apiClass);
		Assertion.checkArgument(Amplifier.class.isAssignableFrom(apiClass), "api class {0} must implement {1}", apiClass, Amplifier.class);
		Assertion.checkNotNull(params);
		//-----
		this.apiClass = apiClass;
		this.params = params;
	}

	/**
	 * @return the api class of the component
	 */
	public Class<? extends Amplifier> getApiClass() {
		return apiClass;
	}

	/**
	 * @return the params
	 */
	public List<Param> getParams() {
		return params;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return apiClass.getName();
	}
}
