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
package io.vertigo.engines.aop.cglib;

import io.vertigo.core.aop.Aspect;
import io.vertigo.core.engines.AopEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

/**
 * @author pchretien
 */
public final class CGLIBAopEngine implements AopEngine {

	/** {@inheritDoc} */
	@Override
	public Object create(final Object instance, final Map<Method, List<Aspect>> joinPoints) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(joinPoints);
		//check : witgh cglib all methods have to bo non-final
		for (final Method method : joinPoints.keySet()) {
			Assertion.checkArgument(!Modifier.isFinal(method.getModifiers()), "due to cglib method '" + method.getName() + "' on '" + instance.getClass().getName() + "' can not be markedf as final");
		}
		//---------------------------------------------------------------------
		final Enhancer enhancer = new Enhancer();
		enhancer.setCallback(createCallBack(instance, joinPoints));
		final Class<?> implClass = instance.getClass();
		final Class[] intfs = ClassUtil.getAllInterfaces(implClass).toArray(new Class[0]);
		enhancer.setInterfaces(intfs);
		enhancer.setSuperclass(implClass);
		return enhancer.create();
	}

	private static Callback createCallBack(final Object instance, final Map<Method, List<Aspect>> joinPoints) {
		return new CGLIBInvocationHandler(instance, joinPoints);
	}

}
