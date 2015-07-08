package io.vertigo.core.spaces.component;

import io.vertigo.core.aop.Aspect;
import io.vertigo.core.config.AspectConfig;
import io.vertigo.core.config.BootConfig;
import io.vertigo.core.config.ComponentConfig;
import io.vertigo.core.config.ModuleConfig;
import io.vertigo.core.config.PluginConfig;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.core.di.reactor.DIReactor;
import io.vertigo.core.engines.AopEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
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
		doInjectComponents(componentSpace, moduleConfig);
		doInjectAspects(componentSpace, moduleConfig);
	}

	private void doInjectComponents(final ComponentSpace componentSpace, final ModuleConfig moduleConfig) {
		final AopEngine aopEngine = bootConfig.getAopEngine();

		final DIReactor reactor = new DIReactor();
		//0; On ajoute la liste des ids qui sont déjà résolus.
		for (final String id : componentSpace.keySet()) {
			reactor.addParent(id);
		}

		//Map des composants définis par leur id
		final Map<String, ComponentConfig> map = new HashMap<>();
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			map.put(componentConfig.getId(), componentConfig);
			//On insère une seule fois un même type de Plugin pour la résolution le plugin
			for (final PluginConfig pluginConfig : componentConfig.getPluginConfigs()) {
				reactor.addComponent(pluginConfig.getId(), pluginConfig.getImplClass(), pluginConfig.getParams().keySet());
			}
			//On insère les plugins puis les composants car les composants dépendent des plugins
			//de sorte on facilite le calcul d'ordre
			reactor.addComponent(componentConfig.getId(), componentConfig.getImplClass(), componentConfig.getParams().keySet());
		}

		final List<String> ids = reactor.proceed();
		//On a récupéré la liste ordonnée des ids.
		for (final String id : ids) {
			if (map.containsKey(id)) {
				final ComponentConfig componentConfig = map.get(id);
				registerComponent(componentSpace, componentConfig, aopEngine);
			}
		}
	}

	private void doInjectAspects(final ComponentSpace componentSpace, final ModuleConfig moduleConfig) {
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

	private void registerComponent(final ComponentSpace componentSpace, final ComponentConfig componentConfig, final AopEngine aopEngine) {
		// 1. On crée et on enregistre les plugins (Qui ne doivent pas dépendre du composant)
		final Map<String, Plugin> plugins = createPlugins(componentSpace, componentConfig);
		componentSpace.registerPlugins(componentConfig.getId(), plugins);

		// 2. On crée l'initializer (Qui ne doit pas dépendre du composant)
		final Option<ComponentInitializer> initializer;
		if (componentConfig.getInitializerClass() != null) {
			initializer = Option.<ComponentInitializer> some(createComponentInitializer(componentSpace, componentConfig));
		} else {
			initializer = Option.none();
		}

		// 3. On crée le composant
		final Object instance = createComponent(bootConfig, componentSpace, componentConfig);

		//4. AOP, on aopise le composant
		final Map<Method, List<Aspect>> joinPoints = ComponentAspectUtil.createJoinPoints(componentConfig, componentSpace.getAspects());
		Object reference;
		if (!joinPoints.isEmpty()) {
			reference = aopEngine.create(instance, joinPoints);
		} else {
			reference = instance;
		}

		// 5. On enregistre le manager et son initializer
		componentSpace.registerComponent(componentConfig.getId(), reference, initializer);
	}

	private static ComponentInitializer<?> createComponentInitializer(final Container componentContainer, final ComponentConfig componentConfig) {
		return Injector.newInstance(componentConfig.getInitializerClass(), componentContainer);
	}

	private static Object createComponent(final BootConfig bootConfig, final Container componentContainer, final ComponentConfig componentConfig) {
		//---pluginTypes
		final Set<String> pluginIds = new HashSet<>();
		for (final PluginConfig pluginConfig : componentConfig.getPluginConfigs()) {
			pluginIds.add(pluginConfig.getId());
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
			for (final String pluginId : pluginIds) {
				if (key.equals(pluginId)) {
					throw new RuntimeException(StringUtil.format("plugin '{0}' on component '{1}' is not used by injection", container.resolve(key, Plugin.class).getClass(), componentConfig));
				}
			}
		}
		//--Search for unuseds params
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

	private static Map<String, Plugin> createPlugins(final Container componentContainer, final ComponentConfig componentConfig) {
		final Map<String, Plugin> plugins = new LinkedHashMap<>();
		for (final PluginConfig pluginConfig : componentConfig.getPluginConfigs()) {
			final Plugin plugin = createPlugin(componentContainer, pluginConfig);
			plugins.put(pluginConfig.getId(), plugin);
		}
		return plugins;
	}

}
