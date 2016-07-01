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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.app.config.ComponentConfig;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.lang.Assertion;

/**
 * Classe permettant d'injecter les intercepteurs sur les points d'exécutions définis dans les aspects.
 * La liste des intercepteurs est construite en amont.
 *
 * Les points d'interceptions sont définis par des annotations placées au niveau
 *  - des classes
 *  - des méthodes.
 *
 * @author pchretien
 */
final class ComponentAspectUtil {
	private ComponentAspectUtil() {
		//
	}

	/**
	 * create all "join points" for a component.
	 * Join points are identifed by a method
	 *
	 * @return Map des aspects par méthode
	 */
	static Map<Method, List<Aspect>> createJoinPoints(final ComponentConfig componentConfig, final Collection<Aspect> aspects) {
		Assertion.checkNotNull(componentConfig);
		Assertion.checkNotNull(aspects);
		//-----
		final Map<Method, List<Aspect>> joinPoints = new HashMap<>();
		for (final Aspect aspect : aspects) {

			// On récupère ttes les méthodes matchant pour l'aspect concerné
			// puis on crée la liste des intercepteurs
			for (final Method method : getMatchingMethods(aspect, componentConfig.getImplClass())) {
				List<Aspect> interceptors = joinPoints.get(method);
				if (interceptors == null) {
					interceptors = new ArrayList<>();
					joinPoints.put(method, interceptors);
				}
				interceptors.add(aspect);
			}
		}
		return joinPoints;
	}

	/*
	 * We are looking all tagged methods to be intercepted.
	 * AspectConfig defines strategy to find these methods in a class.
	 */
	private static Collection<Method> getMatchingMethods(final Aspect aspect, final Class<?> implClass) {
		final Class<?> annotationType = aspect.getAnnotationType();
		final Collection<Method> methods = new ArrayList<>();
		// aspect au niveau classe
		for (final Annotation annotation : implClass.getAnnotations()) {
			if (annotation.annotationType().equals(annotationType)) {
				for (final Method method : implClass.getMethods()) {
					// annotation trouvée, il faut ajouter toutes les méthodes de la classe.
					if (!Object.class.equals(method.getDeclaringClass())) {
						//On ne veut pas des méthodes de Object
						methods.add(method);
					}
				}
			}
		}

		// aspect au niveau méthode
		for (final Method method : implClass.getMethods()) {
			for (final Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType().equals(annotationType)) {
					methods.add(method);
				}
			}
		}
		return methods;
	}
}
