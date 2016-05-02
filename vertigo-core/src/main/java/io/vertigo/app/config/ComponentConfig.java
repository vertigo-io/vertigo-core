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
package io.vertigo.app.config;

import java.util.HashMap;
import java.util.Map;

import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;

/**
 * The componentconfig class defines the configuration of a component.
 *
 * A component is defined by
 *  - an id.
 *  - a implemenation class.
 *  - an optional api class.
 *  - a map of params
 *
 * @author npiedeloup, pchretien
 */
public final class ComponentConfig {
	private final String id;
	private final Class<? extends Component> implClass;
	private final Option<Class<? extends Component>> apiClass;
	private final Map<String, String> params;
	private final boolean elastic;

	/**
	 * Constructor.
	 * @param apiClass api of the component
	 * @param implClass impl class of the component
	 * @param params params
	 */
	ComponentConfig(final Option<Class<? extends Component>> apiClass, final Class<? extends Component> implClass, final boolean elastic, final Map<String, String> params) {
		Assertion.checkNotNull(apiClass);
		Assertion.checkNotNull(implClass);
		Assertion.checkArgument(!apiClass.isPresent() || Component.class.isAssignableFrom(apiClass.get()), "api class {0} must extend {1}", apiClass, Component.class);
		Assertion.checkArgument(Component.class.isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, Component.class);
		Assertion.checkNotNull(params);
		//-----
		id = apiClass.isPresent() ? DIAnnotationUtil.buildId(apiClass.get()) : DIAnnotationUtil.buildId(implClass);
		this.elastic = elastic;
		//-----
		this.apiClass = apiClass;
		this.implClass = implClass;
		this.params = new HashMap<>(params);
	}

	/**
	 * @return impl class of the component
	 */
	public Class<? extends Component> getImplClass() {
		return implClass;
	}

	/**
	 * @return api of the component
	 */
	public Option<Class<? extends Component>> getApiClass() {
		return apiClass;
	}

	/**
	 * @return id of the component
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	public boolean isElastic() {
		return elastic;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return getId();
	}
}
