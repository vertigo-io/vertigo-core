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

import io.vertigo.core.aop.Interceptor;
import io.vertigo.core.lang.Assertion;

/**
 * Définition d'un aspect. 
 * Un aspect est la réunion 
 *  - d'un point d'interception défini par une annotation 
 *  - d'un intercepteur (advice) défini par un composant
 * 
 * @author pchretien
 */
final class AspectConfig {
	/** Annotation à intercepter. */
	private final Class<?> annotationType;
	private final Class<? extends Interceptor> implClass;

	/**
	 * Constructeur.
	 */
	AspectConfig(final Class<?> annotationType, final Class<? extends Interceptor> implClass) {
		Assertion.checkNotNull(annotationType);
		Assertion.checkArgument(annotationType.isAnnotation(), "On attend une annotation '{0}'", annotationType);
		Assertion.checkNotNull(implClass);
		//---------------------------------------------------------------------
		this.annotationType = annotationType;
		this.implClass = implClass;
	}

	/**
	 * @return Type d'Annotation précisant l'interception
	 */
	Class<?> getAnnotationType() {
		return annotationType;
	}

	/**
	 * @return Classe d'implémentation du composant d'interception
	 */
	Class<? extends Interceptor> getInterceptorImplClass() {
		return implClass;
	}
}
