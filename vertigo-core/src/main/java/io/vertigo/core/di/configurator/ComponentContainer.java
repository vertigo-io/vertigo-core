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
package io.vertigo.core.di.configurator;

import io.vertigo.core.Home;
import io.vertigo.core.Logo;
import io.vertigo.core.config.ComponentConfig;
import io.vertigo.core.config.PluginConfig;
import io.vertigo.core.spaces.component.ComponentInitializer;
import io.vertigo.core.spaces.component.Container;
import io.vertigo.core.spaces.component.Plugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Conteneur de tous les gestionnaires.
 * @author pchretien
 */
final class ComponentContainer implements Container, Activeable {
	//On conserve l'ordre d'enregistrement.
	private final Map<String, Object> components = new LinkedHashMap<>();

	//Map des composant démarrés dans l'ordre de démarrage
	private final Map<String, Object> startedComponents = new LinkedHashMap<>();
	private final Map<String, ComponentInitializer> initializers = new HashMap<>();
	private final Map<String, List<Plugin>> subComponents = new LinkedHashMap<>();

	/**
	 * Récupère tous les plugins d'un composant.
	 * @return Liste des plugins
	 */
	List<Plugin> getPlugins(final String componentId) {
		Assertion.checkArgNotEmpty(componentId);
		// ---------------------------------------------------------------------
		return subComponents.get(componentId);
	}

	private void registerComponent(final Object component, final String normalizedId) {
		Assertion.checkArgNotEmpty(normalizedId);
		Assertion.checkNotNull(component);
		//---------------------------------------------------------------------
		//Démarrage du composant
		startComponent(component);
		final Object previous = startedComponents.put(normalizedId, component);
		Assertion.checkState(previous == null, "Composant '{0}' deja enregistré", normalizedId);
	}

	/** {@inheritDoc} */
	public boolean contains(final String id) {
		Assertion.checkArgNotEmpty(id);
		//---------------------------------------------------------------------
		final String normalizedId = StringUtil.normalize(id);
		return startedComponents.containsKey(normalizedId);
	}

	/** {@inheritDoc} */
	public Set<String> keySet() {
		return startedComponents.keySet();
	}

	/** {@inheritDoc} */
	public <C> C resolve(final String id, final Class<C> componentClass) {
		final String normalizedId = StringUtil.normalize(id);
		Assertion.checkArgument(contains(normalizedId), "Aucun composant enregistré pour id = {0} parmi {1}", normalizedId, Home.getComponentSpace().keySet());
		//---------------------------------------------------------------------
		return componentClass.cast(startedComponents.get(normalizedId));
	}

	/**
	 * Enregistrement des plugins .
	 * @param componentClass Classe/Interface du gestionnaire
	 */
	void registerPlugins(final ComponentConfig componentConfig, final Map<PluginConfig, Plugin> plugins) {
		Assertion.checkNotNull(componentConfig);
		Assertion.checkNotNull(plugins);
		// ---------------------------------------------------------------------
		//On crée le container des sous composants (plugins) associés au Manager.
		final Object previous = subComponents.put(componentConfig.getId(), new ArrayList<>(plugins.values()));
		Assertion.checkState(previous == null, "subComponents of component '{0}' deja enregistrés", componentConfig.getId());
		//---------------------------------------------------------------------
		// Il est nécessaire d'enregistrer les sous-composants.

		int nb = 0;
		for (final Entry<PluginConfig, Plugin> entry : plugins.entrySet()) {
			//Attention : il peut y avoir plusieurs plugin d'un même type
			//On enregistre tjrs le premier Plugin de chaque type avec le nom du type de plugin
			String pluginId = entry.getKey().getType();
			if (contains(pluginId)) {
				pluginId += "#" + nb;
			}
			registerComponent(entry.getValue(), pluginId);
			nb++;
		}
	}

	/**
	 * Enregistrement d'un composant.
	 * @param component Gestionnaire
	 * @param apiClass Classe/Interface du gestionnaire
	 */
	void registerComponent(final ComponentConfig componentConfig, final Object component, final Option<ComponentInitializer> componentInitializer) {
		Assertion.checkNotNull(componentConfig);
		Assertion.checkNotNull(component);
		Assertion.checkNotNull(componentInitializer);
		//---On vérifie que le manager est uunique-----------------------------
		final Object old = components.put(componentConfig.getId(), component);
		Assertion.checkState(old == null, "component {0} deja enregistré", componentConfig.getId());
		//---------------------------------------------------------------------
		registerComponent(component, componentConfig.getId());
		if (componentInitializer.isDefined()) {
			initializers.put(componentConfig.getId(), componentInitializer.get());
		}
	}

	/** {@inheritDoc} */
	public void stop() {
		stopComponents();
		clear();
	}

	/** {@inheritDoc} */
	public void start() {
		//le démarrage des composants est effectué au fur et à mesure de leur création.
		//L'initialisation est en revanche globale.
		for (final Entry<String, Object> component : startedComponents.entrySet()) {
			initializeComponent(component.getKey(), component.getValue());
		}
	}

	private static void startComponent(final Object component) {
		final Method startMethod = ComponentLifeCycleUtil.getStartMethod(component.getClass());
		if (startMethod != null) {
			ClassUtil.invoke(component, startMethod);
		}
	}

	private static void stopComponent(final Object component) {
		final Method stopMethod = ComponentLifeCycleUtil.getStopMethod(component.getClass());
		if (stopMethod != null) {
			ClassUtil.invoke(component, stopMethod);
		}
	}

	private <C> void initializeComponent(final String normalizedId, final C component) {
		final ComponentInitializer<C> initializer = initializers.get(normalizedId);
		if (initializer != null) {
			initializer.init(component);
		}
	}

	private void clear() {
		//On nettoie les maps.
		components.clear();
		startedComponents.clear();
		subComponents.clear();
		initializers.clear();
	}

	private void stopComponents() {
		/* Fermeture de tous les gestionnaires.*/
		//On fait les fermetures dans l'ordre inverse des enregistrements.
		//On se limite aux composants qui ont été démarrés.
		final List<Object> reverseComponents = new ArrayList<>(startedComponents.values());
		java.util.Collections.reverse(reverseComponents);

		for (final Object component : reverseComponents) {
			stopComponent(component);
		}
	}

	//=========================================================================
	//======================Gestion des affichages=============================
	//=========================================================================
	void print() {
		// ---Affichage du logo et des modules---
		final PrintStream out = System.out;
		final Logo logo = new Logo();
		logo.printCredits(out);
		out.println();
		print(out);
	}

	/**
	 * Affiche dans la console le logo.
	 * @param out Flux de sortie des informations
	 */
	private void print(final PrintStream out) {
		out.println("####################################################################################################");
		printComponent(out, "Module", "ClassName", "Plugins");
		out.println("# -------------------------+------------------------+----------------------------------------------#");
		//-------------------
		for (final Entry<String, Object> entry : components.entrySet()) {
			printComponent(out, entry.getKey(), entry.getValue());
			out.println("# -------------------------+------------------------+----------------------------------------------#");
		}
		out.println("####################################################################################################");
	}

	private void printComponent(final PrintStream out, final String componentid, final Object component) {
		printComponent(out, componentid, component.getClass().getSimpleName(), null);
		for (final Plugin plugin : getPlugins(componentid)) {
			printComponent(out, null, null, plugin.getClass().getSimpleName());
		}
		//			final ComponentDescription componentDescription = entry.getValue().getDescription();
		//final String info;
		//			if (componentDescription != null && componentDescription.getMainSummaryInfo() != null) {
		//				info = componentDescription.getMainSummaryInfo().getInfo();
		//			} else {
		//info = null;
		//}
		//		printComponent(out, componentClass.getSimpleName(), component.getClass().getSimpleName(), buffer.toString());
	}

	private static void printComponent(final PrintStream out, final String column1, final String column2, final String column3) {
		out.println("# " + truncate(column1, 24) + " | " + truncate(column2, 22) + " | " + truncate(column3, 44) + " #");
	}

	private static String truncate(final String value, final int size) {
		final String result = (value != null ? value : "") + "                                                                  ";
		return result.substring(0, size);
	}
}
