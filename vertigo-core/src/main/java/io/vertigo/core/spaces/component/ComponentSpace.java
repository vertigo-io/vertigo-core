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

import io.vertigo.core.aop.Aspect;
import io.vertigo.core.boot.BootConfig;
import io.vertigo.core.config.AspectConfig;
import io.vertigo.core.config.ComponentConfig;
import io.vertigo.core.config.ModuleConfig;
import io.vertigo.core.config.PluginConfig;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.core.di.reactor.DIReactor;
import io.vertigo.core.engines.AopEngine;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
import io.vertigo.lang.Engine;
import io.vertigo.lang.Option;
import io.vertigo.lang.Plugin;
import io.vertigo.util.StringUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Centralisation des accès aux composants et aux plugins.
 *
 * Les composants et leur initializers sont instanciés par injection
 *  - des paramètres déclarés sur le scope composant.
 *  - des autres composants
 *
 * Les plugins sont instanciés par injection
 *  - des paramètres déclarés sur le scope plugin.
 *  - des autres composants
 *
 * Donc un plugin ne peut pas être injecté dans un plugin, il ne peut être injecté que dans LE composant pour lequel il est prévu.
 * En revanche les composants (à ne pas réaliser de dépendances cycliques) peuvent être injecter dans les composants, les plugins et les initializers.
 *
 * @author pchretien
 */
/**
 * Centralisation des accès aux composants et aux plugins d'un module.
 * Les composants sont d'un type M.
 * @author pchretien
 */
public final class ComponentSpace implements Container, Activeable {
	private final BootConfig bootConfig;
	private final ComponentContainer componentContainer = new ComponentContainer();

	//---Aspects
	private final Map<Class<? extends Aspect>, Aspect> aspects = new LinkedHashMap<>();
	//---/Aspects

	private final List<Engine> engines = new ArrayList<>();

	public ComponentSpace(final BootConfig bootConfig) {
		Assertion.checkNotNull(bootConfig);
		//-----
		this.bootConfig = bootConfig;
	}

	/* We are registered all the components and their plugins*/
	/** {@inheritDoc} */
	@Override
	public void start() {
		if (bootConfig.getElasticaEngine().isDefined()) {
			engines.add(bootConfig.getElasticaEngine().get());
		}

		engines.add(bootConfig.getAopEngine());

		for (final Engine engine : engines) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).start();
			}
		}
		//---
		componentContainer.start();
		if (!bootConfig.isSilence()) {
			//Si on n'est pas en mode silencieux on affiche les infos
			componentContainer.print();
		}
	}

	/*We are stopping all the components.*/
	/** {@inheritDoc} */
	@Override
	public void stop() {
		componentContainer.stop();
		//---
		final List<Engine> reverseEngines = new ArrayList<>(engines);
		java.util.Collections.reverse(reverseEngines);

		for (final Engine engine : reverseEngines) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).stop();
			}
		}
		aspects.clear();
	}

	//	private void startModule(final ModuleConfig moduleConfig, final boolean resource) {
	//		injectComponents(moduleConfig);
	//		injectResources(moduleConfig);
	//	}

	public void injectComponents(final ModuleConfig moduleConfig) {
		final AopEngine aopEngine = bootConfig.getAopEngine();

		final DIReactor reactor = new DIReactor();
		for (final String id : componentContainer.keySet()) {
			reactor.addParent(id); //liste des ids qui sont déjà résolus.
		}

		//Map des composants définis par leur id
		final Map<String, ComponentConfig> map = new HashMap<>();
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			map.put(componentConfig.getId(), componentConfig);
			//On insère une seule fois un même type de Plugin pour la résolution le plugin
			final Set<String> pluginIds = new HashSet<>();
			int nb = 0;
			for (final PluginConfig pluginConfig : componentConfig.getPluginConfigs()) {
				//Attention : il peut y avoir plusieurs plugin d'un même type
				//On enregistre tjrs le premier Plugin de chaque type avec le nom du type de plugin
				String pluginId = pluginConfig.getType();
				if (pluginIds.contains(pluginId)) {
					pluginId += "#" + nb;
				}
				reactor.addComponent(pluginId, pluginConfig.getImplClass(), pluginConfig.getParams().keySet());
				nb++;
				pluginIds.add(pluginId);
			}
			//On insère les plugins puis les composants car les composants dépendent des plugins
			//de sorte on facilite le calcul d'ordre
			reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), componentConfig.getParams().keySet(), pluginIds);
		}

		final List<String> ids = reactor.proceed();
		//On a récupéré la liste ordonnée des ids.
		for (final String id : ids) {
			if (map.containsKey(id)) {
				final ComponentConfig componentConfig = map.get(id);
				registerComponent(componentConfig, aopEngine);
			}
		}

	}

	public void injectAspects(final ModuleConfig moduleConfig) {
		//. On enrichit la liste des aspects
		for (final Aspect aspect : findAspects(moduleConfig)) {
			Assertion.checkArgument(!aspects.containsKey(aspect.getClass()), "aspect {0} already registered", aspect.getClass());
			aspects.put(aspect.getClass(), aspect);
		}
	}

	/**
	 * Find all aspects declared inside a module
	 * @param moduleConfig Module
	 * @return aspects (and its config)
	 */
	private List<Aspect> findAspects(final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		final List<Aspect> findAspects = new ArrayList<>();
		for (final AspectConfig aspectConfig : moduleConfig.getAspectConfigs()) {
			// création de l'instance du composant
			final Aspect aspect = Injector.newInstance(aspectConfig.getAspectImplClass(), this);
			//---
			Assertion.checkNotNull(aspect.getAnnotationType());
			Assertion.checkArgument(aspect.getAnnotationType().isAnnotation(), "On attend une annotation '{0}'", aspect.getAnnotationType());

			findAspects.add(aspect);
		}
		return findAspects;
	}

	private void registerComponent(final ComponentConfig componentConfig, final AopEngine aopEngine) {
		// 1. On crée et on enregistre les plugins (Qui ne doivent pas dépendre du composant)
		final Map<PluginConfig, Plugin> plugins = createPlugins(componentConfig);
		componentContainer.registerPlugins(componentConfig, plugins);

		// 2. On crée l'initializer (Qui ne doit pas dépendre du composant)
		final Option<ComponentInitializer> initializer;
		if (componentConfig.getInitializerClass() != null) {
			initializer = Option.<ComponentInitializer> some(createComponentInitializer(componentConfig));
		} else {
			initializer = Option.none();
		}

		// 3. On crée le composant
		final Object instance = createComponent(componentConfig);

		//4. AOP, on aopise le composant
		final Map<Method, List<Aspect>> joinPoints = ComponentAspectUtil.createJoinPoints(componentConfig, aspects.values());
		Object reference;
		if (!joinPoints.isEmpty()) {
			reference = aopEngine.create(instance, joinPoints);
		} else {
			reference = instance;
		}

		// 5. On enregistre le manager et son initializer
		componentContainer.registerComponent(componentConfig, reference, initializer);
	}

	private ComponentInitializer<?> createComponentInitializer(final ComponentConfig componentConfig) {
		return Injector.newInstance(componentConfig.getInitializerClass(), componentContainer);
	}

	private Object createComponent(final ComponentConfig componentConfig) {
		//---pluginTypes
		final Set<String> pluginTypes = new HashSet<>();
		for (final PluginConfig pluginConfig : componentConfig.getPluginConfigs()) {
			pluginTypes.add(pluginConfig.getType());
		}
		//---
		if (componentConfig.isElastic()) {
			return bootConfig.getElasticaEngine().get().createProxy(componentConfig.getApiClass().get());
		}
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(componentConfig.getParams());
		final ComponentDualContainer container = new ComponentDualContainer(componentContainer, paramsContainer);
		//---
		final Object component = Injector.newInstance(componentConfig.getImplClass(), container);
		//--Search for unuseds plugins
		// We are inspecting all unused keys, and we check if we can find almost one plugin of the component.
		for (final String key : container.getUnusedKeys()) {
			for (final String pluginType : pluginTypes) {
				if (key.startsWith(pluginType)) {
					throw new RuntimeException(StringUtil.format("plugin '{0}' on component '{1}' is not used by injection", container.resolve(key, Plugin.class).getClass(), componentConfig));
				}
			}
		}
		//--Search for unuseds params
		Assertion.checkState(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}'", paramsContainer.getUnusedKeys());
		return component;
	}

	private Plugin createPlugin(final PluginConfig pluginConfig) {
		final ComponentParamsContainer paramsContainer = new ComponentParamsContainer(pluginConfig.getParams());
		final Container container = new ComponentDualContainer(componentContainer, paramsContainer);
		//---
		final Plugin plugin = Injector.newInstance(pluginConfig.getImplClass(), container);
		Assertion.checkState(paramsContainer.getUnusedKeys().isEmpty(), "some params are not used :'{0}'", paramsContainer.getUnusedKeys());
		return plugin;
	}

	private Map<PluginConfig, Plugin> createPlugins(final ComponentConfig componentConfig) {
		final Map<PluginConfig, Plugin> plugins = new LinkedHashMap<>();
		for (final PluginConfig pluginConfig : componentConfig.getPluginConfigs()) {
			final Plugin plugin = createPlugin(pluginConfig);
			plugins.put(pluginConfig, plugin);
		}
		return plugins;
	}

	/**
	 * @param componentClass Classe type du composant(Interface)
	 * @param <T> Type du composant
	 * @return Gestionnaire centralisé des documents.
	 */
	public <T> T resolve(final Class<T> componentClass) {
		final String normalizedId = StringUtil.normalize(componentClass.getSimpleName());
		return componentContainer.resolve(normalizedId, componentClass);
	}

	/** {@inheritDoc} */
	@Override
	public <T> T resolve(final String id, final Class<T> componentClass) {
		return componentContainer.resolve(id, componentClass);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return componentContainer.keySet();
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		return componentContainer.contains(id);
	}
}
