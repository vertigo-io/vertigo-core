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
package io.vertigo.core.util;

import io.vertigo.core.component.Plugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author prahmoune
 */
public final class DIAnnotationUtil {
	private DIAnnotationUtil() {
		//Classe utilitaire, constructeur est privé.
	}

	/**
	 * Récupération du constructeur. 
	 * Il doit y avoir 1 et un seul constructeur public
	 * Ce constructeur doit être vide ou marqué avec l'annotation @Inject.
	 * @param clazz Class de l'objet
	 * @return Constructeur de l'objet
	 */
	public static <T> Constructor<T> findInjectableConstructor(final Class<T> clazz) {
		Assertion.checkNotNull(clazz);
		//---------------------------------------------------------------------
		final Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
		Assertion.checkNotNull(constructors, "Aucun constructeur public identifiable");
		Assertion.checkArgument(constructors.length == 1, "Un seul constructeur public doit être déclaré sur {0}", clazz.getName());
		Assertion.checkArgument(isInjectable(constructors[0]), "Le constructeur public de {0} doit être marqué avec l'annotation @Inject ou bien être vide", clazz.getName());
		//-----------------------------------------------------------------

		//On a un et un seul constructeur.
		return constructors[0];
	}

	private static boolean isInjectable(final Constructor<?> constructor) {
		return constructor.getParameterTypes().length == 0 || constructor.isAnnotationPresent(Inject.class);
	}

	/**
	 * Indique si le i-éme paramètre du constructeur est optionnel.
	 * @param constructor Constructeur testé
	 * @param i indice du paramètre
	 * @return Si le i-éme paramètre du contructeur est optionnel.
	 */
	public static boolean isOptional(final Constructor<?> constructor, final int i) {
		Assertion.checkNotNull(constructor);
		//---------------------------------------------------------------------
		return Option.class.isAssignableFrom(constructor.getParameterTypes()[i]);
	}

	/**
	 * Indique si le champ du composant est optionnel.
	 * @param field Champ du composant
	 * @return Si ce champ est optionnel.
	 */
	public static boolean isOptional(final Field field) {
		Assertion.checkNotNull(field);
		//---------------------------------------------------------------------
		return Option.class.isAssignableFrom(field.getType());
	}

	/**
	 * Indique Indique si le i-éme paramètre du constructeur est une liste de plugins.
	 * @param constructor Constructeur testé
	 * @param i indice du paramètre
	 * @return Si le i-éme paramètre du contructeur une liste de plugins.
	 */
	public static boolean hasPlugins(final Constructor<?> constructor, final int i) {
		Assertion.checkNotNull(constructor);
		//---------------------------------------------------------------------
		if (List.class.isAssignableFrom(constructor.getParameterTypes()[i])) {
			if (!Plugin.class.isAssignableFrom(ClassUtil.getGeneric(constructor, i))) {
				throw new IllegalStateException("Only plugins can be injected in list");
			}
			return true;
		}
		return false;
	}

	/**
	 * Indique si le champ du composant correspond à une liste de plugins.
	 * @param field Champ du composant
	 * @return Si ce champ est optionnel.
	 */
	public static boolean hasPlugins(final Field field) {
		Assertion.checkNotNull(field);
		//---------------------------------------------------------------------
		if (List.class.isAssignableFrom(field.getType())) {
			if (!Plugin.class.isAssignableFrom(ClassUtil.getGeneric(field))) {
				throw new IllegalStateException("Only plugins can be injected in list");
			}
			return true;
		}
		return false;
	}

	/**
	 * @return Création de l'identifiant du composant
	 */
	public static String buildId(final Option<Class<?>> apiClass, final Class<?> implClass) {
		Assertion.checkNotNull(apiClass);
		Assertion.checkNotNull(implClass);
		//---------------------------------------------------------------------
		if (apiClass.isDefined()) {
			//if en api is defined, api muust define id 
			final String id = buildId(apiClass.get());
			if (implClass.isAnnotationPresent(Named.class)) {
				//if an api is defined and an annotation is found on implementation then we have to check the consistency
				final Named named = implClass.getAnnotation(Named.class);
				Assertion.checkArgument(id.equals(named.value()), "Name of component '{0}'is ambiguous, 'named' annotation on implementation conflict with api", apiClass.get());
			}
			return id;
		}
		return buildId(implClass);
	}

	/**
	 * Construction d'un ID pour un composant défini par une implémentation.	
	 * @param clazz Classe d'implémentation du composant
	 * @return Identifiant du composant 
	 */
	public static String buildId(final Class<?> clazz) {
		Assertion.checkNotNull(clazz);
		//---------------------------------------------------------------------
		//On construit l'identifiant du composant.
		//Par ordre de priorité l'id est 
		// - la valeur de l'annotation Named si il y a une annotation Named déclarée
		// - Sinon on prend le nom de la classe passée en paramètre.
		if (clazz.isAnnotationPresent(Named.class)) {
			//Si le composant recherché n'est pas explicitement précisé alors on le recherche via son type 
			//et dans ce cas son id est obligatoirement le nom complet de la classe ou de l'interface.
			final Named named = clazz.getAnnotation(Named.class);
			return named.value();
		}
		return getId(clazz);
	}

	private static String getId(final Class<?> implClass) {
		//Par convention on prend le nom de la classe.
		return StringUtil.normalize(implClass.getSimpleName());
	}

	/**
	 * Construction d'un ID pour un champ (Les options sont autorisées).
	 * @param field Champ du composant (Option autorisée)
	 * @return Identifiant du composant 
	 */
	public static String buildId(final Field field) {
		Assertion.checkNotNull(field);
		//---------------------------------------------------------------------
		final Class<?> implClass;
		final String named = getNamedValue(field.getAnnotations());
		if (Option.class.isAssignableFrom(field.getType())) {
			implClass = ClassUtil.getGeneric(field);
		} else if (List.class.isAssignableFrom(field.getType())) {
			implClass = ClassUtil.getGeneric(field);
			Assertion.checkArgument(Plugin.class.isAssignableFrom(implClass), "Only plugins can be injected in list");
			Assertion.checkState(named == null, "List of plugins can not be named");
		} else {
			implClass = field.getType();
		}

		//Si le champ est une option alors on prend le type de l'option.
		return named != null ? named : getId(implClass);
	}

	/**
	 * Construction d'un ID pour le i-éme paramètre du constructeur (Les options sont autorisées).
	 * @param constructor Constructeur
	 * @param i indice du paramètre
	 * @return Identifiant du composant
	 */
	public static String buildId(final Constructor<?> constructor, final int i) {
		Assertion.checkNotNull(constructor);
		//---------------------------------------------------------------------
		final String named = getNamedValue(constructor.getParameterAnnotations()[i]);

		final Class<?> implClass;
		if (Option.class.isAssignableFrom(constructor.getParameterTypes()[i])) {
			implClass = ClassUtil.getGeneric(constructor, i);
		} else if (List.class.isAssignableFrom(constructor.getParameterTypes()[i])) {
			implClass = ClassUtil.getGeneric(constructor, i);
			Assertion.checkArgument(Plugin.class.isAssignableFrom(implClass), "Only plugins can be injected in list");
			Assertion.checkState(named == null, "List of plugins can not be named");
		} else {
			implClass = constructor.getParameterTypes()[i];
		}
		return named != null ? named : getId(implClass);
	}

	private static String getNamedValue(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (annotation instanceof Named) {
				return Named.class.cast(annotation).value();
			}
		}
		return null;
	}
}
