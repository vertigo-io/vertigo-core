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
package io.vertigo.kernel.util;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * @author prahmoune
 */
public final class DILifeCycleUtil {
	private static final Method postConstructMethod = buildStartMethod();
	private static final Method preDestroyMethod = buildStopMethod();

	private static Method buildStartMethod() {
		return ClassUtil.findMethod(Activeable.class, "start", new Class[] {});
	}

	private static Method buildStopMethod() {
		return ClassUtil.findMethod(Activeable.class, "stop", new Class[] {});
	}

	private DILifeCycleUtil() {
		//Constructeur priv� car classe utilitaire.
	}

	public static Method getStartMethod(final Class<?> clazz) {
		Assertion.checkNotNull(clazz);
		//---------------------------------------------------------------------
		final Method startMethod = getMethod(clazz, PostConstruct.class);

		if (Activeable.class.isAssignableFrom(clazz)) {
			Assertion.checkState(startMethod == null, "Ambiguit� sur la m�thode de d�marrage sur {0}", clazz);
			return postConstructMethod;
		}
		return startMethod;
	}

	public static Method getStopMethod(final Class<?> clazz) {
		Assertion.checkNotNull(clazz);
		//---------------------------------------------------------------------
		final Method stopMethod = getMethod(clazz, PreDestroy.class);
		if (Activeable.class.isAssignableFrom(clazz)) {
			Assertion.checkState(stopMethod == null, "Ambiguit� sur la m�thode d'arr�t sur {0}", clazz);
			return preDestroyMethod;
		}
		return stopMethod;
	}

	private static Method getMethod(final Class<?> clazz, final Class<? extends Annotation> annotation) {
		for (final Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				if (method.getParameterTypes().length > 0) {
					throw new VRuntimeException("Life cycle method '" + method + "' should not have any parameters");
				}
				return method;
			}
		}
		//Si on ne trouve pas de m�thode avec l'annotation souhait�e
		return null;
	}
}
