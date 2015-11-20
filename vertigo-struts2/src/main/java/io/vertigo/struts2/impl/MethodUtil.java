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
package io.vertigo.struts2.impl;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Container;
import io.vertigo.lang.Option;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Named;

/**
 * Gestion du passage de paramètres aux Actions.
 * @author npiedeloup
 */
public final class MethodUtil {

	private MethodUtil() {
		//privé pour une classe utilitaire
	}

	/**
	 * Invocation dynamique d'une méthode sur une instance.
	 *
	 * @param instance Objet sur lequel est invoqu� la méthode
	 * @param methodName Nom de la methode invoqu�e (la premiere trouvée est appelée)
	 * @param container Container des arguments
	 * @return R Valeur retournée par l'invocation
	 */
	public static Object invoke(final Object instance, final String methodName, final Container container) {
		final Option<Method> actionMethod = findMethodByName(instance.getClass(), methodName);
		if (actionMethod.isEmpty()) {
			throw new VSystemException("Méthode {0} non trouvée sur {1}", methodName, instance.getClass().getName());
		}
		actionMethod.get().setAccessible(true); //la méthode peut être protected
		return invoke(instance, actionMethod.get(), container);
	}

	/**
	 * Invocation dynamique d'une méthode sur une instance.
	 *
	 * @param instance Objet sur lequel est invoqu� la méthode
	 * @param method Methode invoqu�e
	 * @param container Container des arguments
	 * @return R Valeur retournée par l'invocation
	 */
	public static Object invoke(final Object instance, final Method method, final Container container) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(method);
		//-----
		final Object[] args = findMethodParameters(container, method);
		return ClassUtil.invoke(instance, method, args);
	}

	/**
	 * Retrouve une méthode par son nom.
	 * Part de la class d�clarante et remonte les superclass.
	 * @param declaringClass Class de la méthode
	 * @param methodName Nom de la méthode
	 * @return Option de la première méthode trouvée.
	 */
	public static Option<Method> findMethodByName(final Class<?> declaringClass, final String methodName) {
		for (final Method method : declaringClass.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return Option.some(method);
			}
		}
		if (declaringClass.getSuperclass() != null) {
			return findMethodByName(declaringClass.getSuperclass(), methodName);
		}
		return Option.none();
	}

	private static Object[] findMethodParameters(final Container container, final Method method) {
		final Object[] parameters = new Object[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			parameters[i] = getInjected(container, method, i);
		}
		return parameters;
	}

	//On récupère pour le paramètre i du constructeur l'objet à injecter
	private static Object getInjected(final Container container, final Method method, final int i) {
		final String id = getNamedValue(method.getParameterAnnotations()[i]);
		//-----
		final boolean optionalParameter = isOptional(method, i);
		if (optionalParameter) {
			if (container.contains(id)) {
				return Option.some(container.resolve(id, ClassUtil.getGeneric(method, i)));
			}
			return Option.none();
		}
		final Object value = container.resolve(id, method.getParameterTypes()[i]);
		Assertion.checkNotNull(value);
		//-----
		return value;
	}

	private static boolean isOptional(final Method method, final int i) {
		Assertion.checkNotNull(method);
		//-----
		return Option.class.isAssignableFrom(method.getParameterTypes()[i]);
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
