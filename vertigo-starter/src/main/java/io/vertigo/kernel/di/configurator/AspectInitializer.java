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

import io.vertigo.kernel.Home;
import io.vertigo.kernel.aop.Interceptor;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.kernel.lang.Assertion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Classe permettant d'injecter les intercepteurs sur les points d'ex�cutions d�finis dans les aspects.
 * La liste des intercepteurs est construite en amont.
 * 
 * Les points d'interceptions sont d�finis par des annotations plac�es au niveau 
 *  - des classes
 *  - des m�thodes.
 *  
 * @author pchretien, prahmoune
 * @version $Id: AspectInitializer.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
final class AspectInitializer {
	private final Map<AspectConfig, Interceptor> interceptorsMap;
	private final List<AspectConfig> aspectConfigs;

	AspectInitializer(final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//---------------------------------------------------------------------
		// On construit la Map des intercepteurs utilisables.
		interceptorsMap = createInterceptorsMap(moduleConfig.getAspectConfigs());
		aspectConfigs = moduleConfig.getAspectConfigs();
	}

	/**
	 * Cr�ation des composants
	 * @param adviceInfos Liste des advices � cr�er
	 * @param componentRefFactory Factory des r�f�rences utilisables 
	 * @return Liste des composants
	 */
	private static Map<AspectConfig, Interceptor> createInterceptorsMap(final Collection<AspectConfig> aspectInfos) {
		final Injector injector = new Injector();

		final Map<AspectConfig, Interceptor> interceptorMap = new HashMap<>();
		for (final AspectConfig aspectInfo : aspectInfos) {
			// cr�ation de l'instance du composant
			final Interceptor interceptor = injector.newInstance(aspectInfo.getInterceptorImplClass(), Home.getComponentSpace());
			interceptorMap.put(aspectInfo, interceptor);
		}
		return interceptorMap;
	}

	/**
	 * Cr�ation des points d'ex�cution identifi�s par m�thode.
	 * 
	 * @param implClass Classe portant les aspects
	 * @return Map des aspects par m�thode
	 */
	Map<Method, List<Interceptor>> createJoinPoints(final ComponentConfig componentConfig) {
		final Map<Method, List<Interceptor>> joinPoints = new HashMap<>();
		for (final AspectConfig aspectInfo : aspectConfigs) {
			// Build the interceptor list
			final Interceptor advice = interceptorsMap.get(aspectInfo);

			// On r�cup�re ttes les m�thodes matchant pour l'aspect concern�
			// puis on cr�e la liste des intercepteurs
			for (final Method method : getMatchingMethods(aspectInfo.getAnnotationType(), componentConfig.getImplClass())) {
				List<Interceptor> interceptors = joinPoints.get(method);
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
				// annotation trouv�e, il faut ajouter toutes les m�thodes de la classe.
				methods.addAll(Arrays.asList(implClass.getMethods()));
			}
		}

		// aspect au niveau m�thode
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
