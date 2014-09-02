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
package io.vertigo.core.di.reactor;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.kernel.util.DIAnnotationUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

/**
 * Modèle d'un composant.
 * Un composant est défini par ses dépendances externes.
 * Les dépendances à des objets fournis par les params ne sont pas exposées. (ele ne servent pas dans la résolution). 
 * @author prahmoune, pchretien
 */
final class DIComponentInfo {
	private final String id;
	private final Collection<DIDependency> dependencies;

	DIComponentInfo(final String id, final Class<?> implClass, final Set<String> pluginIds, final Set<String> params) {
		Assertion.checkArgNotEmpty(id);
		//		Assertion.precondition(Container.REGEX_ID.matcher(id).matches(), "id '{0}' doit être camelCase et commencer par une minuscule", id);
		Assertion.checkNotNull(implClass);
		Assertion.checkNotNull(params);
		//---------------------------------------------------------------------
		this.id = id;
		dependencies = buildDependencies(this, implClass, params, pluginIds);
	}

	String getId() {
		return id;
	}

	Collection<DIDependency> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		//Utilisé pour afficher les messages d'erreurs lors du calcul des DI
		return id;
	}

	//-------------------------------------------------------------------------
	//------------------------Calcul des Dépendances---------------------------
	//-------------------------------------------------------------------------
	private static Collection<DIDependency> buildDependencies(final DIComponentInfo diComponentInfo, final Class<?> implClass, final Set<String> params, final Set<String> pluginIds) {
		final Collection<DIDependency> tmpDependencies = new ArrayList<>();
		//Les paramètres sont supposés connus et ne sont donc pas concernés par l'analyse de dépendances
		populateConstructorDepedencies(diComponentInfo, tmpDependencies, implClass, params);
		populateFieldDepencies(diComponentInfo, tmpDependencies, implClass, params);
		populatePluginDepedencies(diComponentInfo, tmpDependencies, pluginIds);
		return Collections.unmodifiableList(new ArrayList<>(tmpDependencies));
	}

	/**
	 * Dependencies on each plugin
	 */
	private static void populatePluginDepedencies(final DIComponentInfo diComponentInfo, final Collection<DIDependency> dependencies, final Set<String> pluginIds) {
		for (final String pluginId : pluginIds) {
			dependencies.add(new DIDependency(diComponentInfo, pluginId));
		}
	}

	/**
	 * Dependencies on constructor
	 * @param diComponentInfo 
	 */
	private static void populateConstructorDepedencies(final DIComponentInfo diComponentInfo, final Collection<DIDependency> dependencies, final Class<?> implClass, final Set<String> params) {
		final Constructor<?> constructor = DIAnnotationUtil.findInjectableConstructor(implClass);
		//On construit la liste de ses dépendances.
		for (int i = 0; i < constructor.getParameterTypes().length; i++) {
			final DIDependency dependency = new DIDependency(diComponentInfo, constructor, i);
			if (!params.contains(dependency.getId())) {
				dependencies.add(dependency);
			}
		}
	}

	/**
	 * Dependencies on each field
	 */
	private static void populateFieldDepencies(final DIComponentInfo diComponentInfo, final Collection<DIDependency> dependencies, final Class<?> implClass, final Set<String> params) {
		final Collection<Field> fields = ClassUtil.getAllFields(implClass, Inject.class);
		for (final Field field : fields) {
			//On utilise le build sur les champs avec les options autorisées.
			final DIDependency dependency = new DIDependency(diComponentInfo, field);
			if (!params.contains(dependency.getId())) {
				dependencies.add(dependency);
			}
		}
	}
}
