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
package io.vertigo.core.spaces.component;

import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.core.component.di.reactor.DIReactor;
import io.vertigo.core.config.AspectConfig;
import io.vertigo.core.config.BootConfig;
import io.vertigo.core.config.ComponentConfig;
import io.vertigo.core.config.ModuleConfig;
import io.vertigo.core.config.PluginConfig;
import io.vertigo.core.engines.AopEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
import io.vertigo.lang.Option;
import io.vertigo.lang.Plugin;
import io.vertigo.util.StringUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pchretien
 */
public final class ComponentLoader {
	private final BootConfig bootConfig;

	public ComponentLoader(final BootConfig bootConfig) {
		Assertion.checkNotNull(bootConfig);
		//-----
		this.bootConfig = bootConfig;
	}

	public void injectAllComponents(final ComponentSpace componentSpace, final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		for (final ModuleConfig moduleConfig : moduleConfigs) {
			injectComponent(componentSpace, moduleConfig);
		}
	}

	public void injectBootComponents(final ComponentSpace componentSpace) {
		injectComponent(componentSpace, bootConfig.getBootModuleConfig());

	}

	private void injectComponent(final ComponentSpace componentSpace, final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		doInjectComponents(bootConfig, componentSpace, moduleConfig);
		doInjectAspects(componentSpace, moduleConfig);
	}

	private static void doInjectComponents(final BootConfig bootConfig, final ComponentSpace componentSpace, final ModuleConfig moduleConfig) {
		final AopEngine aopEngine = bootConfig.getAopEngine();

		final DIReactor reactor = new DIReactor();
		//0; On ajoute la liste des ids qui sont déjà résolus.
		for (final String id : componentSpace.keySet()) {
			reactor.addParent(id);
		}

		//Map des composants définis par leur id
		final Map<String, ComponentConfig> componentConfigById = new HashMap<>();
		final Map<String, PluginConfig> pluginConfigById = new HashMap<>();

		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			componentConfigById.put(componentConfig.getId(), componentConfig);
			reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), componentConfig.getParams().keySet());
		}

		for (final PluginConfig pluginConfig : moduleConfig.getPluginConfigs()) {
			pluginConfigById.put(pluginConfig.getId(), pluginConfig);
			reactor.addComponent(pluginConfig.getId(), pluginConfig.getImplClass(), pluginConfig.getParams().keySet());
		}
		//Comment trouver des plugins orphenlins ?

		final List<String> ids = reactor.proceed();

		//On a récupéré la liste ordonnée des ids.
		//On positionne un proxy pour compter les plugins non utilisés
		final ComponentProxyContainer componentContainer = new ComponentProxyContainer(componentSpace);

		for (final String id : ids) {
			if (componentConfigById.containsKey(id)) {
				//Si il s'agit d'un comoposant
				final ComponentConfig componentConfig = componentConfigById.get(id);
				// 2.a On crée le composant avec AOP et autres options (elastic)
				final Object component = createComponentWithOptions(bootConfig, componentContainer, componentSpace.getAspects(), componentConfig, aopEngine);
				// 2.b On crée l'initializer (Qui ne doit pas dépendre du composant)
				final Option<ComponentInitializer> initializer = createComponentInitializer(componentSpace, componentConfig);
				// 2.c. On enregistre le composant avec son initializer
				componentSpace.registerComponent(componentConfig.getId(), component, initializer);
			} else {
				//Il s'agit d'un plugin
				final PluginConfig pluginConfig = pluginConfigById.get(id);
				final Plugin plugin = createPlugin(componentSpace, pluginConfig);
				final Option<ComponentInitializer> initializer = Option.none();
				componentSpace.registerComponent(pluginConfig.getId(), plugin, initializer);
			}
		}

		//---
		//--Search for unuseds plugins
		// We are removing all used keys from the map of PluginConfig, and we check if we can find almost one plugin of the component.
		for (final String pluginId : componentContainer.getUsedKeys()) {
			pluginConfigById.remove(pluginId);
		}

		if (!pluginConfigById.isEmpty()) {
			throw new RuntimeException(StringUtil.format("plugins '{0}' in module'{1}' are not used by injection", pluginConfigById.values(), moduleConfig));
		}
	}

	private static void doInjectAspects(final ComponentSpace componentSpace, final ModuleConfig moduleConfig) {
		//. On enrichit la liste des aspects
		for (final Aspect aspect : findAspects(componentSpace, moduleConfig)) {
			componentSpace.registerAspect(aspect);
		}
	}

	/**
	 * Find all aspects declared inside a module
	 * @param moduleConfig Module
	 * @return aspects (and its config)
	 */
	private static List<Aspect> findAspects(final Container container, final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		final List<Aspect> findAspects = new ArrayList<>();
		for (final AspectConfig aspectConfig : moduleConfig.getAspectConfigs()) {
			// création de l'instance du composant
			final Aspect aspect = Injector.newInstance(aspectConfig.getAspectImplClass(), container);
			//---
			Assertion.checkNotNull(aspect.getAnnotationType());
			Assertion.checkArgument(aspect.getAnnotationType().isAnnotation(), "On attend une annotation '{0}'", aspect.getAnnotationType());

			findAspects.add(aspect);
		}
		return findAspects;
	}

	private static Object createComponentWithOptions(final BootConfig bootConfig, final ComponentProxyContainer componentContainer, final Collection<Aspect> aspects, final ComponentConfig componentConfig, final AopEngine aopEngine) {
		// 2. On crée le composant
		final Object instance = createComponent(bootConfig, componentContainer, componentConfig);

		//3. AOP, on aopise le composant
		final Map<Method, List<Aspect>> joinPoints = ComponentAspectUtil.createJoinPoints(componentConfig, aspects);
		if (!joinPoints.isEmpty()) {
			return aopEngine.create(instance, joinPoints);
		}
		return instance;
	}

	private static final Option<ComponentInitializer> createComponentInitializer(final Container componentContainer, final ComponentConfig componentConfig) {
		if (componentConfig.getInitializerClass() != null) {
			final ComponentInitializer<?> componentInitializer = Injector.newInstance(componentConfig.getInitializerClass(), componentContainer);
			return Option.<ComponentInitializer> some(componentInitializer);
		}
		return Option.none();
	}

	private static Object createComponent(final BootConfig bootConfig, final ComponentProxyContainer componentContainer, final ComponentConfig componentConfig) {
		if (componentConfig.isElastic()) {
			return bootConfig.getElasticaEngine().get().createProxy(componentConfig.getApiClass().get());
		}
		//---
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(componentConfig.getParams());
		final ComponentDualContainer container = new ComponentDualContainer(componentContainer, paramsContainer);
		//---
		final Object component = Injector.newInstance(componentConfig.getImplClass(), container);
		Assertion.checkState(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}'", paramsContainer.getUnusedKeys());
		return component;
	}

	private static Plugin createPlugin(final Container componentContainer, final PluginConfig pluginConfig) {
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(pluginConfig.getParams());
		final Container container = new ComponentDualContainer(componentContainer, paramsContainer);
		//---
		final Plugin plugin = Injector.newInstance(pluginConfig.getImplClass(), container);
		Assertion.checkState(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}'", paramsContainer.getUnusedKeys());
		return plugin;
	}
}
