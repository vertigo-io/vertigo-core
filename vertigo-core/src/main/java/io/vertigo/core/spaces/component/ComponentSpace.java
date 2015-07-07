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

import io.vertigo.core.Home;
import io.vertigo.core.Logo;
import io.vertigo.core.aop.Aspect;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
import io.vertigo.lang.Option;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	//---Aspects
	private final Map<Class<? extends Aspect>, Aspect> aspects = new LinkedHashMap<>();

	//On conserve l'ordre d'enregistrement. (plugins + pure components)
	private final Map<String, Object> components = new LinkedHashMap<>();

	//Map des composant démarrés dans l'ordre de démarrage
	private final Map<String, Object> startedComponents = new LinkedHashMap<>();
	private final Map<String, ComponentInitializer> initializers = new HashMap<>();
	private final Map<String, List<Plugin>> pluginsByComponentId = new LinkedHashMap<>();

	private final boolean silently;

	public ComponentSpace(final boolean silently) {
		this.silently = silently;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//le démarrage des composants est effectué au fur et à mesure de leur création.
		//L'initialisation est en revanche globale.
		for (final Entry<String, Object> component : startedComponents.entrySet()) {
			initializeComponent(component.getKey(), component.getValue());
		}
		if (!silently) {
			print();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		stopComponents();
		clear();

	}

	Collection<Aspect> getAllAspects() {
		return aspects.values();
	}

	boolean containsAspect(final Class<? extends Aspect> aspectClass) {
		return aspects.containsKey(aspectClass);
	}

	void registerAspect(final Aspect aspect) {
		aspects.put(aspect.getClass(), aspect);
	}

	/**
	 * @param componentClass Classe type du composant(Interface)
	 * @param <T> Type du composant
	 * @return Gestionnaire centralisé des documents.
	 */
	public <T> T resolve(final Class<T> componentClass) {
		final String normalizedId = StringUtil.first2LowerCase(componentClass.getSimpleName());
		return resolve(normalizedId, componentClass);
	}

	private void registerComponent(final String componentId, final Object component) {
		Assertion.checkArgNotEmpty(componentId);
		Assertion.checkNotNull(component);
		//-----
		//Démarrage du composant
		startComponent(component);
		final Object previous = startedComponents.put(componentId, component);
		Assertion.checkState(previous == null, "Composant '{0}' deja enregistré", componentId);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		Assertion.checkArgNotEmpty(id);
		//-----
		final String normalizedId = StringUtil.first2LowerCase(id);
		return startedComponents.containsKey(normalizedId);
	}

	/** {@inheritDoc} */
	@Override
	public <C> C resolve(final String id, final Class<C> componentClass) {
		final String normalizedId = StringUtil.first2LowerCase(id);
		Assertion.checkArgument(contains(normalizedId), "Aucun composant enregistré pour id = {0} parmi {1}", normalizedId, Home.getComponentSpace().keySet());
		//-----
		return componentClass.cast(startedComponents.get(normalizedId));
	}

	/**
	 * Enregistrement des plugins .
	 */
	void registerPlugins(final String componentId, final Map<String, Plugin> plugins) {
		Assertion.checkNotNull(componentId);
		Assertion.checkNotNull(plugins);
		//-----
		//On crée le container des sous composants (plugins) associés au Manager.
		final Object previous = pluginsByComponentId.put(componentId, new ArrayList<>(plugins.values()));
		Assertion.checkState(previous == null, "subComponents of component '{0}' deja enregistrés", componentId);
		//-----
		// Il est nécessaire d'enregistrer tous les plugins.
		for (final Entry<String, Plugin> entry : plugins.entrySet()) {
			registerComponent(entry.getKey(), entry.getValue());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return startedComponents.keySet();
	}

	/**
	 * Enregistrement d'un composant.
	 * @param component Gestionnaire
	 */
	void registerComponent(final String componentId, final Object component, final Option<ComponentInitializer> componentInitializer) {
		Assertion.checkNotNull(componentId);
		Assertion.checkNotNull(component);
		Assertion.checkNotNull(componentInitializer);
		//On vérifie que le manager est unique
		final Object old = components.put(componentId, component);
		Assertion.checkState(old == null, "component {0} deja enregistré", componentId);
		//-----
		registerComponent(componentId, component);
		if (componentInitializer.isDefined()) {
			initializers.put(componentId, componentInitializer.get());
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
		pluginsByComponentId.clear();
		initializers.clear();
		aspects.clear();
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
	private void print() {
		// ---Affichage du logo et des modules---
		final PrintStream out = System.out;
		Logo.printCredits(out);
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
		//-----
		for (final Entry<String, Object> entry : components.entrySet()) {
			printComponent(out, entry.getKey(), entry.getValue());
			out.println("# -------------------------+------------------------+----------------------------------------------#");
		}
		out.println("####################################################################################################");
	}

	private void printComponent(final PrintStream out, final String componentId, final Object component) {
		printComponent(out, componentId, component.getClass().getSimpleName(), null);
		for (final Plugin plugin : pluginsByComponentId.get(componentId)) {
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
