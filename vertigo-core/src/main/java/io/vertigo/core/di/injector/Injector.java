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
package io.vertigo.core.di.injector;

import io.vertigo.core.di.DIAnnotationUtil;
import io.vertigo.core.di.DIException;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
import io.vertigo.lang.Option;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Injector.
 * Create new instances.
 * Warning : Activeable méthods (preDestroy and PostConstruct) are not managed by Injector. 
 *
 * @author pchretien
 */
public final class Injector {
	private Injector() {
		//constructor is protected, Injector contains only static methods
	}

	/**
	 * Injection de dépendances.
	 * Création d'une instance  à partir d'un conteneur de composants déjà intsanciés.
	 *
	 * @param <T> Type de l'instance
	 * @param clazz Classe de l'instance
	 * @param container Fournisseur de composants
	 * @return Instance de composants créée.
	 */
	public static <T> T newInstance(final Class<T> clazz, final Container container) {
		Assertion.checkNotNull(clazz);
		Assertion.checkNotNull(container);
		//-----
		//On encapsule la création par un bloc try/ctach afin de préciser le type de composant qui n'a pas pu être créé.
		try {
			final T instance = createInstance(clazz, container);
			injectMembers(instance, container);
			return instance;
		} catch (final Exception e) {
			//Contextualisation de l'exception et des assertions.
			throw new DIException("Erreur lors de la création du composant de type : '" + clazz.getName() + "'", e);
		}
	}

	private static <T> T createInstance(final Class<T> clazz, final Container container) {
		//On a un et un seul constructeur public injectable.
		final Constructor<T> constructor = DIAnnotationUtil.findInjectableConstructor(clazz);
		//On recherche les paramètres
		final Object[] constructorParameters = findConstructorParameters(container, constructor);
		return ClassUtil.newInstance(constructor, constructorParameters);
	}

	/**
	 * Injection des propriétés dans une instance.
	 */
	public static void injectMembers(final Object instance, final Container container) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(container);
		//-----
		final Collection<Field> fields = ClassUtil.getAllFields(instance.getClass(), Inject.class);
		for (final Field field : fields) {
			final Object injected = getInjected(container, field);
			//On vérifie que si il s'agit d'un champ non primitif alors ce champs n'avait pas été initialisé
			Assertion.checkState(field.getType().isPrimitive() || null == ClassUtil.get(instance, field), "field '{0}' is already initialized", field);
			ClassUtil.set(instance, field, injected);
		}
	}

	private static Object[] findConstructorParameters(final Container container, final Constructor<?> constructor) {
		final Object[] parameters = new Object[constructor.getParameterTypes().length];
		for (int i = 0; i < constructor.getParameterTypes().length; i++) {
			parameters[i] = getInjected(container, constructor, i);
		}
		return parameters;
	}

	//On récupère pour le paramètre i du constructeur l'objet à injecter
	private static Object getInjected(final Container container, final Constructor<?> constructor, final int i) {
		final String id = DIAnnotationUtil.buildId(constructor, i);
		final Class<?> type = constructor.getParameterTypes()[i];
		//-----
		final boolean isOption = DIAnnotationUtil.isOption(type);
		final boolean isList = DIAnnotationUtil.isList(type);
		final Class<?> genericType = (isOption || isList) ? ClassUtil.getGeneric(constructor, i) : null;
		//-----
		return getInjected(container, id, type, isOption, isList, genericType);
	}

	//On récupère pour le champ 'field' l'objet à injecter
	private static Object getInjected(final Container container, final Field field) {
		final String id = DIAnnotationUtil.buildId(field);
		final Class<?> type = field.getType();
		//-----
		final boolean isOption = DIAnnotationUtil.isOption(type);
		final boolean isList = DIAnnotationUtil.isList(type);
		final Class<?> genericType = (isOption || isList) ? ClassUtil.getGeneric(field) : null;

		return getInjected(container, id, type, isOption, isList, genericType);
	}

	private static Object getInjected(final Container container, final String id, final Class<?> type, final boolean isOption, final boolean isList, final Class<?> genericType) {
		if (isOption) {
			if (container.contains(id)) {
				//On récupère la valeur et on la transforme en option.
				//ex : <param name="opt-port" value="a value that can be null or not">
				return Option.option(container.resolve(id, genericType));
			}
			//
			return Option.none();
		}
		//Injection des listes de plugins
		if (isList) {
			final String pluginIdPrefix = DIAnnotationUtil.buildId(genericType);

			//on récupère la liste des plugin du type concerné
			final List<Plugin> list = new ArrayList<>();
			for (final String pluginId : container.keySet()) {
				//On prend tous les plugins du type concerné
				if (pluginId.equals(pluginIdPrefix) || pluginId.startsWith(pluginIdPrefix + '#')) {
					final Plugin injected = container.resolve(pluginId, Plugin.class);
					Assertion.checkArgument(genericType.isAssignableFrom(injected.getClass()), "type of {0} is incorrect ; expected : {1}", pluginId, genericType.getName());
					list.add(injected);
				}
			}
			return Collections.unmodifiableList(list);
		}
		//-----
		final Object value = container.resolve(id, type);
		Assertion.checkNotNull(value);
		//-----
		return value;
	}

}
