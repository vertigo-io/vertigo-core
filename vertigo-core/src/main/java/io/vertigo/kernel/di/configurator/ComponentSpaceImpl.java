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
package io.vertigo.kernel.di.configurator;

import io.vertigo.kernel.Engine;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.aop.Interceptor;
import io.vertigo.kernel.command.VCommand;
import io.vertigo.kernel.command.VCommandExecutor;
import io.vertigo.kernel.component.ComponentInitializer;
import io.vertigo.kernel.component.ComponentSpace;
import io.vertigo.kernel.component.Container;
import io.vertigo.kernel.component.ParamsContainer;
import io.vertigo.kernel.component.Plugin;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.kernel.di.reactor.Reactor;
import io.vertigo.kernel.engines.AopEngine;
import io.vertigo.kernel.engines.VCommandEngine;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.metamodel.DefinitionSpace;
import io.vertigo.kernel.resource.ResourceLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

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
public final class ComponentSpaceImpl implements ComponentSpace {
	public static final ComponentSpace EMPTY = new ComponentSpaceImpl(new ComponentSpaceConfigBuilder().build());

	private final ComponentSpaceConfig componentSpaceConfig;
	private final ComponentContainer componentContainer = new ComponentContainer();
	private final Injector injector = new Injector();

	private List<Engine> engines = new ArrayList<>();

	public ComponentSpaceImpl(ComponentSpaceConfig componentSpaceConfig) {
		Assertion.checkNotNull(componentSpaceConfig);
		//---------------------------------------------------------------------
		this.componentSpaceConfig = componentSpaceConfig;
	}

	/**
	 * Enregistrement des composants et de leurs plugins.
	 */
	public void start() {
		initLog(componentSpaceConfig.getParams());
		registeComponents();
		// --------------------------
		if (componentSpaceConfig.getElasticaEngine().isDefined()) {
			engines.add(componentSpaceConfig.getElasticaEngine().get());
		}
		if (componentSpaceConfig.getRestEngine().isDefined()) {
			engines.add(componentSpaceConfig.getRestEngine().get());
		}

		engines.add(componentSpaceConfig.getAopEngine());

		for (Engine engine : engines) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).start();
			}
		}
		//
		for (ModuleConfig moduleConfig : componentSpaceConfig.getModuleConfigs()) {
			//			int resourcesToBeLoad = moduleConfig.getResourceConfigs().size();
			//We are doing a copy of all resources, to check that they are all parsed. 
			List<ResourceConfig> resourceConfigsToDo = new ArrayList<>(moduleConfig.getResourceConfigs());
			for (ResourceLoader resourceLoader : Home.getResourceSpace().getResourceLoaders()) {
				//Candidates contins all resources that can be treated by the resourceLoader
				List<ResourceConfig> candidates = new ArrayList<>();
				for (Iterator<ResourceConfig> it = resourceConfigsToDo.iterator(); it.hasNext();) {
					final ResourceConfig resourceConfig = it.next();
					if (resourceLoader.getTypes().contains(resourceConfig.getType())) {
						candidates.add(resourceConfig);
						it.remove();
					}
				}
				resourceLoader.parse(candidates);
			}
			Assertion.checkArgument(resourceConfigsToDo.isEmpty(), "All resources '{1}' have not been parsed successfully : {}", resourceConfigsToDo);
		}
		//---	
		componentContainer.start();
		if (!componentSpaceConfig.isSilence()) {
			//Si on n'est pas en mode silencieux on affiche les infos
			componentContainer.print();
		}

		//VCommandEngine must be started after the container
		if (componentSpaceConfig.getCommandEngine().isDefined()) {
			final VCommandEngine commandEngine = componentSpaceConfig.getCommandEngine().get();
			if (commandEngine instanceof Activeable) {
				((Activeable) commandEngine).start();
			}
			//			engines.add(commandEngine);
			//		}
			//
			//		if (componentSpaceConfig.getCommandEngine().isDefined()) {
			commandEngine.registerCommandExecutor("config", new VCommandExecutor<ComponentSpaceConfig>() {
				public ComponentSpaceConfig exec(VCommand command) {
					return componentSpaceConfig;
				}
			});

			commandEngine.registerCommandExecutor("definitions", new VCommandExecutor<DefinitionSpace>() {
				/** {@inheritDoc} */
				public DefinitionSpace exec(VCommand command) {
					Assertion.checkNotNull(command);
					//---------------------------------------------------------------------
					return Home.getDefinitionSpace();
				}
			});
		}
	}

	/**
	 * Arret de tous les composants.
	 */
	public void stop() {
		componentContainer.stop();
		//---
		List<Engine> reverseEngines = new ArrayList<>(engines);
		java.util.Collections.reverse(reverseEngines);

		for (Engine engine : reverseEngines) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).stop();
			}
		}
	}

	private void initLog(final Map<String, String> params) {
		final String log4jFileName = params.get("log4j.configurationFileName");
		if (log4jFileName != null) {
			final boolean log4jFormatXml = log4jFileName.endsWith(".xml");
			final URL url = getClass().getResource(log4jFileName);
			if (url != null) {
				if (log4jFormatXml) {
					DOMConfigurator.configure(url);
				} else {
					PropertyConfigurator.configure(url);
				}
				Logger.getRootLogger().info("Log4J configuration chargée (resource) : " + url.getFile());
			} else {
				Assertion.checkArgument(new File(log4jFileName).exists(), "Fichier de configuration log4j : {0} est introuvable", log4jFileName);
				// Avec configureAndWatch (utilise un anonymous thread)
				// on peut modifier à chaud le fichier de conf log4j
				// mais en cas de hot-deploy, le thread reste présent ce qui peut-entrainer des problèmes.
				if (log4jFormatXml) {
					DOMConfigurator.configureAndWatch(log4jFileName);
				} else {
					PropertyConfigurator.configureAndWatch(log4jFileName);
				}
			}
			Logger.getRootLogger().info("Log4J configuration chargée (fichier) : " + log4jFileName);
		}
	}

	private void registeComponents() {
		final AopEngine aopEngine = componentSpaceConfig.getAopEngine();

		final List<String> solved = new ArrayList<>();
		for (final ModuleConfig moduleConfig : componentSpaceConfig.getModuleConfigs()) {
			final Reactor reactor = new Reactor();
			//Map des composants définis par leur id
			final Map<String, ComponentConfig> map = new HashMap<>();

			for (final String id : solved) {
				reactor.addParent(id);
			}

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
					reactor.addComponent(pluginId, pluginConfig.getImplClass(), Collections.<String> emptySet(), pluginConfig.getParams().keySet());
					nb++;
					pluginIds.add(pluginId);
				}
				//On insère les plugins puis les composants car les composants dépendent des plugins
				//de sorte on facilite le calcul d'ordre
				reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), pluginIds, componentConfig.getParams().keySet());
			}

			final List<String> ids = reactor.proceed();
			//On a récupéré la liste ordonnée des ids.

			//. On initialise l'injecteur AOP
			final AspectInitializer aspectInitializer = new AspectInitializer(moduleConfig);

			for (final String id : ids) {
				if (map.containsKey(id)) {
					final ComponentConfig componentConfig = map.get(id);
					registerComponent(componentConfig, aspectInitializer, aopEngine);
				}
			}
			solved.addAll(ids);
		}
	}

	private void registerComponent(final ComponentConfig componentConfig, final AspectInitializer aspectInitializer, final AopEngine aopEngine) {
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
		final Map<Method, List<Interceptor>> joinPoints = aspectInitializer.createJoinPoints(componentConfig);
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
		final Container container = new DualContainer(componentContainer, new ParamsContainer(componentConfig.getParams()));
		return injector.newInstance(componentConfig.getInitializerClass(), container);
	}

	private Object createComponent(final ComponentConfig componentConfig) {
		if (componentConfig.isElastic()) {
			return componentSpaceConfig.getElasticaEngine().get().createProxy(componentConfig.getApiClass().get());
		}
		final Container container = new DualContainer(componentContainer, new ParamsContainer(componentConfig.getParams()));
		return injector.newInstance(componentConfig.getImplClass(), container);
	}

	private Plugin createPlugin(final PluginConfig pluginConfig) {
		final Container container = new DualContainer(componentContainer, new ParamsContainer(pluginConfig.getParams()));
		return injector.newInstance(pluginConfig.getImplClass(), container);
	}

	private Map<PluginConfig, Plugin> createPlugins(final ComponentConfig componentConfig) {
		final Map<PluginConfig, Plugin> plugins = new LinkedHashMap<>();
		for (final PluginConfig pluginConfig : componentConfig.getPluginConfigs()) {
			final Plugin plugin = createPlugin(pluginConfig);
			plugins.put(pluginConfig, plugin);
		}
		return plugins;
	}

	/** {@inheritDoc} */
	public <T> T resolve(final Class<T> componentClass) {
		return componentContainer.resolveComponent(componentClass);
	}

	@Override
	public <T> T resolve(final String id, final Class<T> componentClass) {
		return componentContainer.resolve(id, componentClass);
	}

	/** {@inheritDoc} */
	public Set<String> keySet() {
		return componentContainer.keySet();
	}

	/** {@inheritDoc} */
	public boolean contains(final String id) {
		return componentContainer.contains(id);
	}

	/** {@inheritDoc} */
	public ComponentSpaceConfig getConfig() {
		return componentSpaceConfig;
	}

}
