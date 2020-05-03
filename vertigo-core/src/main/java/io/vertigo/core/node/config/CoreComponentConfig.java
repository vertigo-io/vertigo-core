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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Amplifier;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;

/**
 * The componentconfig class defines the configuration of a component.
 *
 * A component is defined by
 *  - an id.
 *  - proxy or pure component -no proxy-
 *  - a implemenation class (empty if proxy, required if not proxy)
 *  - an optional api class. (required if proxy)
 *  - a map of params
 *
 * @author npiedeloup, pchretien
 */
public final class CoreComponentConfig {
	private static enum Type {
		COMPONENT, PLUGIN, AMPLIFIER, CONNECTOR
	}

	private final Type type;
	private final String id;
	private final Optional<Class<? extends CoreComponent>> implClassOpt;
	private final Optional<Class<? extends CoreComponent>> apiClassOpt;
	private final Map<String, String> params;

	static CoreComponentConfig createComponent(String id, Optional<Class<? extends Component>> apiClassOpt, final Class<? extends Component> implClass, List<Param> params) {
		Optional<Class<? extends CoreComponent>> implClassOpt = Optional.of(implClass);
		Optional<Class<? extends CoreComponent>> myApiClassOpt = Optional.ofNullable(apiClassOpt.orElse(null));
		return new CoreComponentConfig(Type.COMPONENT, id, myApiClassOpt, implClassOpt, params);
	}

	static CoreComponentConfig createPlugin(String id, final Class<? extends Plugin> implClass, List<Param> params) {
		final Optional<Class<? extends CoreComponent>> apiClassOpt = Optional.empty();
		Optional<Class<? extends CoreComponent>> implClassOpt = Optional.of(implClass);
		return new CoreComponentConfig(Type.PLUGIN, id, apiClassOpt, implClassOpt, params);
	}

	static CoreComponentConfig createAmplifier(String id, final Class<? extends Amplifier> apiClass, List<Param> params) {
		final Optional<Class<? extends CoreComponent>> apiClassOpt = Optional.of(apiClass);
		Optional<Class<? extends CoreComponent>> implClassOpt = Optional.empty();
		return new CoreComponentConfig(Type.AMPLIFIER, id, apiClassOpt, implClassOpt, params);
	}

	static CoreComponentConfig createConnector(String id, final Class<? extends Connector> implClass, List<Param> params) {
		final Optional<Class<? extends CoreComponent>> apiClassOpt = Optional.empty();
		Optional<Class<? extends CoreComponent>> implClassOpt = Optional.of(implClass);
		return new CoreComponentConfig(Type.CONNECTOR, id, apiClassOpt, implClassOpt, params);
	}

	/**
	 * Constructor.
	 * @param apiClassOpt api of the component
	 * @param implClass impl class of the component
	 * @param params params
	 */
	private CoreComponentConfig(
			final Type type,
			final String id,
			final Optional<Class<? extends CoreComponent>> apiClassOpt,
			final Optional<Class<? extends CoreComponent>> implClassOpt,
			final List<Param> params) {
		Assertion.check()
				.notNull(type)
				.argNotEmpty(id)
				.notNull(apiClassOpt)
				.notNull(implClassOpt)
				.notNull(params);
		//--
		this.type = type;
		switch (type) {
			case AMPLIFIER:
				Assertion.check()
						.argument(!implClassOpt.isPresent(), "When an amplifier is declared there is no impl")
						.argument(apiClassOpt.isPresent(), "When an amplifier is declared, an api is required")
						.argument(Amplifier.class.isAssignableFrom(apiClassOpt.get()), "An amplifier must inherit Amplifier");
				break;
			case COMPONENT:
				Assertion.when(apiClassOpt.isPresent()).check(() -> CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class);
				Assertion.check()
						.argument(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class));
				break;
			case CONNECTOR:
				Assertion.when(apiClassOpt.isPresent()).check(() -> CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class);
				Assertion.when(apiClassOpt.isPresent()).check(() -> CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class);
				Assertion.check()
						.argument(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class));
				break;
			case PLUGIN:
				Assertion.when(apiClassOpt.isPresent()).check(() -> CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class);
				Assertion.when(apiClassOpt.isPresent()).check(() -> CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class);
				Assertion.check()
						.argument(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class));
				break;
			default:
				throw new IllegalStateException();
		}
		if (type == Type.AMPLIFIER) {
			Assertion.check()
					.argument(!implClassOpt.isPresent(), "When a proxy is declared there is no impl")
					.argument(apiClassOpt.isPresent(), "When a proxy is declared, an api is required")
					.argument(Amplifier.class.isAssignableFrom(apiClassOpt.get()), "An amplifier must inherit Amplifier");
		} else {
			Assertion.check()
					.argument(implClassOpt.isPresent(), "When a classic component -no proxy-  is declared, an impl is required")
					.argument(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class));
			Assertion.when(apiClassOpt.isPresent()).check(() -> CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class);
		}
		//-----
		this.id = id;
		this.apiClassOpt = apiClassOpt;
		this.implClassOpt = implClassOpt;

		this.params = params
				.stream()
				.collect(Collectors.toMap(Param::getName, Param::getValue));
	}

	/**
	 * @return impl class of the component
	 */
	public Class<? extends CoreComponent> getImplClass() {
		//Assertion.checkState(!proxy, "a proxy has no impl");
		//---
		return implClassOpt.get();
	}

	/**
	 * @return api of the component
	 */
	public Optional<Class<? extends CoreComponent>> getApiClass() {
		return apiClassOpt;
	}

	/**
	 * @return id of the component
	 */
	public String getId() {
		return id;
	}

	/*
	 * Return type of the Core Component
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return if the component is a proxy
	 */
	public boolean isProxy() {
		return type == Type.AMPLIFIER;
	}

	/**
	 * @return params
	 */
	public Map<String, String> getParams() {
		return params;
	}
}
