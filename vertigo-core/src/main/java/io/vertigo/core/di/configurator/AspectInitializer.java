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
import io.vertigo.core.aop.AOPInterceptor;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.core.lang.Assertion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe permettant d'injecter les intercepteurs sur les points d'exécutions définis dans les aspects.
 * La liste des intercepteurs est construite en amont.
 * 
 * Les points d'interceptions sont définis par des annotations placées au niveau 
 *  - des classes
 *  - des méthodes.
 *  
 * @author pchretien, prahmoune
 */
final class AspectInitializer {
	private final Map<AspectConfig, AOPInterceptor> interceptorsMap;
	private final List<AspectConfig> aspectConfigs;

	AspectInitializer(final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//---------------------------------------------------------------------
		// On construit la Map des intercepteurs utilisables.
		interceptorsMap = createInterceptorsMap(moduleConfig.getAspectConfigs());
		aspectConfigs = moduleConfig.getAspectConfigs();
	}

	/**
	 * Création des composants
	 * @param adviceInfos Liste des advices à créer
	 * @param componentRefFactory Factory des références utilisables 
	 * @return Liste des composants
	 */
	private static Map<AspectConfig, AOPInterceptor> createInterceptorsMap(final Collection<AspectConfig> aspectInfos) {
		final Injector injector = new Injector();

		final Map<AspectConfig, AOPInterceptor> interceptorMap = new HashMap<>();
		for (final AspectConfig aspectInfo : aspectInfos) {
			// création de l'instance du composant
			final AOPInterceptor interceptor = injector.newInstance(aspectInfo.getInterceptorImplClass(), Home.getComponentSpace());
			interceptorMap.put(aspectInfo, interceptor);
		}
		return interceptorMap;
	}

	/**
	 * Création des points d'exécution identifiés par méthode.
	 * 
	 * @param implClass Classe portant les aspects
	 * @return Map des aspects par méthode
	 */
	Map<Method, List<AOPInterceptor>> createJoinPoints(final ComponentConfig componentConfig) {
		final Map<Method, List<AOPInterceptor>> joinPoints = new HashMap<>();
		for (final AspectConfig aspectInfo : aspectConfigs) {
			// Build the interceptor list
			final AOPInterceptor advice = interceptorsMap.get(aspectInfo);

			// On récupère ttes les méthodes matchant pour l'aspect concerné
			// puis on crée la liste des intercepteurs
			for (final Method method : getMatchingMethods(aspectInfo.getAnnotationType(), componentConfig.getImplClass())) {
				List<AOPInterceptor> interceptors = joinPoints.get(method);
				if (interceptors == null) {
					interceptors = new ArrayList<>();
					joinPoints.put(method, interceptors);
				}
				interceptors.add(advice);
			}
		}
		return joinPoints;
	}

	private static Collection<Method> getMatchingMethods(final Class<?> annotationType, final Class<?> implClass) {
		final Collection<Method> methods = new ArrayList<>();
		// aspect au niveau classe
		for (final Annotation annotation : implClass.getAnnotations()) {
			if (annotation.annotationType().equals(annotationType)) {
				for (Method method : implClass.getMethods()) {
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
