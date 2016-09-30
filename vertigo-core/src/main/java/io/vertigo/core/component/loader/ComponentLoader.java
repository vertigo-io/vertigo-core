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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.config.AspectConfig;
import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.core.component.di.reactor.DIReactor;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.spaces.component.ComponentSpace;
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

	/**
	 * Constructor.
	 * @param aopPlugin the plugin which is reponsible for the aop strategy
	 */
	@Inject
	public ComponentLoader(final AopPlugin aopPlugin) {
		Assertion.checkNotNull(aopPlugin);
		//-----
		this.aopPlugin = aopPlugin;
	}

	/**
	 * Add all the components defined in the moduleConfigs into the componentSpace.
	 *
	 * @param componentSpace Space where all the components are stored
	 * @param paramManager Manager of params
	 * @param moduleConfigs Configs of modules to add.
	 */
	public void injectAllComponents(final ComponentSpace componentSpace, final ParamManager paramManager, final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		for (final ModuleConfig moduleConfig : moduleConfigs) {
			injectComponent(componentSpace, Optional.of(paramManager), moduleConfig);
		}
	}

	/**
	 * Add all the components depending on the boot module.
	 * @param componentSpace Space where all the components are stored
	 * @param bootModuleConfig Configs of the boot module
	 */
	public void injectBootComponents(final ComponentSpace componentSpace, final ModuleConfig bootModuleConfig) {
		doInjectComponents(componentSpace, Optional.<ParamManager> empty(), bootModuleConfig);
		Assertion.checkArgument(bootModuleConfig.getAspectConfigs().isEmpty(), "boot module can't contain aspects");
		Assertion.checkArgument(bootModuleConfig.getDefinitionProviderConfigs().isEmpty(), "boot module can't contain definitions");
		Assertion.checkArgument(bootModuleConfig.getDefinitionResourceConfigs().isEmpty(), "boot module can't contain definitions");
		//Dans le cas de boot il n,'y a ni initializer, ni aspects, ni definitions
	}

	private void injectComponent(final ComponentSpace componentSpace, final Optional<ParamManager> paramManagerOption, final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		doInjectComponents(componentSpace, paramManagerOption, moduleConfig);
		doInjectAspects(componentSpace, moduleConfig);
	}

	private void doInjectComponents(final ComponentSpace componentSpace, final Optional<ParamManager> paramManagerOption, final ModuleConfig moduleConfig) {
		final DIReactor reactor = new DIReactor();
		//0; On ajoute la liste des ids qui sont déjà résolus.
		for (final String id : componentSpace.keySet()) {
			reactor.addParent(id);
		}

		//Map des composants définis par leur id
		final Map<String, ComponentConfig> componentConfigById = moduleConfig.getComponentConfigs()
				.stream()
				.peek(componentConfig -> reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), componentConfig.getParams().keySet()))
				.collect(Collectors.toMap(ComponentConfig::getId, Function.identity()));

		//Comment trouver des plugins orphenlins ?

		final List<String> ids = reactor.proceed();

		//On a récupéré la liste ordonnée des ids.
		//On positionne un proxy pour compter les plugins non utilisés
		final ComponentProxyContainer componentContainer = new ComponentProxyContainer(componentSpace);

		for (final String id : ids) {
			if (componentConfigById.containsKey(id)) {
				//Si il s'agit d'un composant (y compris plugin)
				final ComponentConfig componentConfig = componentConfigById.get(id);
				// 2.a On crée le composant avec AOP et autres options (elastic)
				final Component component = createComponentWithOptions(paramManagerOption, componentContainer, componentConfig);
				// 2.b. On enregistre le composant
				componentSpace.registerComponent(componentConfig.getId(), component);
			}
		}

		//---
		//--Search for unuseds plugins
		final List<String> unusedPluginIds = moduleConfig.getComponentConfigs()
				.stream()
				.filter(componentConfig -> Plugin.class.isAssignableFrom(componentConfig.getImplClass()))
				//only plugins are considered
				.map(pluginConfig -> pluginConfig.getId())
				//used keys are removed
				.filter(pluginId -> !componentContainer.getUsedKeys().contains(pluginId))
				.collect(Collectors.toList());

		if (!unusedPluginIds.isEmpty()) {
			throw new VSystemException("plugins '{0}' in module'{1}' are not used by injection", unusedPluginIds, moduleConfig);
		}
	}

	private void doInjectAspects(final Container container, final ModuleConfig moduleConfig) {
		//. On enrichit la liste des aspects
		for (final Aspect aspect : findAspects(container, moduleConfig)) {
			registerAspect(aspect);
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
		return moduleConfig.getAspectConfigs()
				.stream()
				.map(aspectConfig -> createAspect(container, aspectConfig))
				.collect(Collectors.toList());
	}

	private static Aspect createAspect(final Container container, final AspectConfig aspectConfig) {
		// création de l'instance du composant
		final Aspect aspect = Injector.newInstance(aspectConfig.getAspectImplClass(), container);
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
			return aopPlugin.create(instance, joinPoints);
		}
		return instance;
	}

	private static <C extends Component> C createInstance(final Container componentContainer, final Optional<ParamManager> paramManagerOption, final ComponentConfig componentConfig) {
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(paramManagerOption, componentConfig.getParams());
		final Container container = new ComponentDualContainer(componentContainer, paramsContainer);
		//---
		final C component = (C) Injector.newInstance(componentConfig.getImplClass(), container);
		Assertion.checkState(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}' in plugin '{1}'", paramsContainer.getUnusedKeys(), componentConfig.getId());
		return component;
	}

	private Component createComponentWithOptions(final Optional<ParamManager> paramManagerOption, final ComponentProxyContainer componentContainer, final ComponentConfig componentConfig) {
		// 1. An instance is created
		final Component instance = createInstance(componentContainer, paramManagerOption, componentConfig);

		//2. AOP , a new instance is created when aspects are injected in the previous instance
		return injectAspects(instance, componentConfig.getImplClass());
	}

}
