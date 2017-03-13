/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.app.config.AspectConfig;
import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.ComponentSpaceWritable;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.core.component.di.reactor.DIReactor;
import io.vertigo.core.param.ParamManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.Container;
import io.vertigo.lang.Plugin;
import io.vertigo.lang.VSystemException;

/**
 * The componentLoader class defines the way to load the components defined in the config into componentSpace.
 * @author pchretien
 */
public final class ComponentLoader {
	private final AopPlugin aopPlugin;
	/** Aspects.*/
	private final List<Aspect> aspects = new ArrayList<>();
	private final ComponentSpaceWritable componentSpace;

	/**
	 * Constructor.
	 * @param componentSpace Space where all the components are stored
	 * @param aopPlugin the plugin which is reponsible for the aop strategy
	 */
	public ComponentLoader(final ComponentSpaceWritable componentSpace, final AopPlugin aopPlugin) {
		Assertion.checkNotNull(componentSpace);
		Assertion.checkNotNull(aopPlugin);
		//-----
		this.componentSpace = componentSpace;
		this.aopPlugin = aopPlugin;
	}

	/**
	 * Add all the components defined in the moduleConfigs into the componentSpace.
	 * @param optionalParamManager optional paramManager
	 * @param moduleConfigs the config of the module to add.
	 */
	public void injectAllComponentsAndAspects(final Optional<ParamManager> optionalParamManager, final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		for (final ModuleConfig moduleConfig : moduleConfigs) {
			injectComponents(optionalParamManager, moduleConfig.getName(), moduleConfig.getComponentConfigs());
			injectAspects(moduleConfig.getAspectConfigs());
		}
	}

	/**
	 * Adds all the components defined by theur configs.
	 * @param paramManagerOption the optional manager of params
	 * @param moduleName the name of the module
	 * @param componentConfigs the configs of the components
	 */
	public void injectComponents(final Optional<ParamManager> paramManagerOption, final String moduleName, final List<ComponentConfig> componentConfigs) {
		Assertion.checkNotNull(componentSpace);
		Assertion.checkNotNull(paramManagerOption);
		Assertion.checkNotNull(moduleName);
		Assertion.checkNotNull(componentConfigs);
		//---
		final DIReactor reactor = new DIReactor();
		//0; On ajoute la liste des ids qui sont déjà résolus.
		for (final String id : componentSpace.keySet()) {
			reactor.addParent(id);
		}

		//Map des composants définis par leur id
		final Map<String, ComponentConfig> componentConfigById = componentConfigs
				.stream()
				.peek(componentConfig -> reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), componentConfig.getParams().keySet()))
				.collect(Collectors.toMap(ComponentConfig::getId, Function.identity()));

		//Comment trouver des plugins orphenlins ?

		final List<String> ids = reactor.proceed();

		//On a récupéré la liste ordonnée des ids.
		//On positionne un proxy pour compter les plugins non utilisés
		final ComponentProxyContainer componentProxyContainer = new ComponentProxyContainer(componentSpace);

		for (final String id : ids) {
			if (componentConfigById.containsKey(id)) {
				//Si il s'agit d'un composant (y compris plugin)
				final ComponentConfig componentConfig = componentConfigById.get(id);
				// 2.a On crée le composant avec AOP et autres options (elastic)
				final Component component = createComponentWithOptions(paramManagerOption, componentProxyContainer, componentConfig);
				// 2.b. On enregistre le composant
				componentSpace.registerComponent(componentConfig.getId(), component);
			}
		}

		//---
		//--Search for unuseds plugins
		final List<String> unusedPluginIds = componentConfigs
				.stream()
				.filter(componentConfig -> Plugin.class.isAssignableFrom(componentConfig.getImplClass()))
				//only plugins are considered
				.map(ComponentConfig::getId)
				//used keys are removed
				.filter(pluginId -> !componentProxyContainer.getUsedKeys().contains(pluginId))
				.collect(Collectors.toList());

		if (!unusedPluginIds.isEmpty()) {
			throw new VSystemException("plugins '{0}' in module'{1}' are not used by injection", unusedPluginIds, moduleName);
		}
	}

	private void injectAspects(final List<AspectConfig> aspectConfigs) {
		//. On enrichit la liste des aspects
		findAspects(componentSpace, aspectConfigs)
				.forEach(this::registerAspect);
	}

	/**
	 * Find all aspects declared inside a module
	 * @param aspectConfigs the list of all aspects inside the module
	 * @return aspects (and its config)
	 */
	private static Stream<Aspect> findAspects(final Container container, final List<AspectConfig> aspectConfigs) {
		Assertion.checkNotNull(aspectConfigs);
		//-----
		return aspectConfigs
				.stream()
				.map(aspectConfig -> createAspect(container, aspectConfig));
	}

	private static Aspect createAspect(final Container container, final AspectConfig aspectConfig) {
		// création de l'instance du composant
		final Aspect aspect = DIInjector.newInstance(aspectConfig.getAspectClass(), container);
		//---
		Assertion.checkNotNull(aspect.getAnnotationType());
		return aspect;
	}

	private void registerAspect(final Aspect aspect) {
		Assertion.checkNotNull(aspect);
		Assertion.checkArgument(aspects.stream().noneMatch(a -> a.getClass().equals(aspect.getClass())), "aspect {0} already registered with the same class", aspect.getClass());
		Assertion.checkArgument(aspects.stream().noneMatch(a -> a.getAnnotationType().equals(aspect.getAnnotationType())), "aspect {0} already registered with the same annotation", aspect.getClass());
		//-----
		aspects.add(aspect);
	}

	private <C extends Component> C injectAspects(final C instance, final Class implClass) {
		//2. AOP , a new instance is created when aspects are injected in the previous instance
		final Map<Method, List<Aspect>> joinPoints = ComponentAspectUtil.createJoinPoints(implClass, aspects);
		if (!joinPoints.isEmpty()) {
			return aopPlugin.wrap(instance, joinPoints);
		}
		return instance;
	}

	private static <C extends Component> C createInstance(final Container container, final Optional<ParamManager> paramManagerOption, final ComponentConfig componentConfig) {
		return (C) createInstance(componentConfig.getImplClass(), container, paramManagerOption, componentConfig.getParams());
	}

	private Component createComponentWithOptions(final Optional<ParamManager> paramManagerOption, final ComponentProxyContainer componentContainer, final ComponentConfig componentConfig) {
		// 1. An instance is created
		final Component instance = createInstance(componentContainer, paramManagerOption, componentConfig);

		//2. AOP , a new instance is created when aspects are injected in the previous instance
		return injectAspects(instance, componentConfig.getImplClass());
	}

	/**
	 * Creates a component that use the injector but adds params support.
	 * @param clazz the clazz of the object to create
	 * @param container the container of the known components
	 * @param paramManagerOption the optional ParamManager needed to use global params resolution
	 * @param params the local params
	 * @return the component created
	 */
	public static <T> T createInstance(final Class<T> clazz, final Container container, final Optional<ParamManager> paramManagerOption, final Map<String, String> params) {
		Assertion.checkNotNull(paramManagerOption);
		Assertion.checkNotNull(params);
		// ---
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(paramManagerOption, params);
		final Container dualContainer = new ComponentDualContainer(container, paramsContainer);
		//---
		final T component = DIInjector.newInstance(clazz, dualContainer);
		Assertion.checkState(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}' in component '{1}'", paramsContainer.getUnusedKeys(), clazz);
		return component;
	}

}
