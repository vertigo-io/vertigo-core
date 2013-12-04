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
package io.vertigoimpl.engines.aop.cglib;

import io.vertigo.kernel.aop.Interceptor;
import io.vertigo.kernel.engines.AopEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

/**
 * @author pchretien
 */
public final class CGLIBAopEngine implements AopEngine {

	/** {@inheritDoc} */
	public Object create(final Object instance, final Map<Method, List<Interceptor>> interceptors) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(interceptors);
		//---------------------------------------------------------------------
		final Enhancer enhancer = new Enhancer();
		enhancer.setCallback(createCallBack(instance, interceptors));
		final Class<?> implClass = instance.getClass();
		final Class<?>[] intfs = ClassUtil.getAllInterfaces(implClass);
		enhancer.setInterfaces(intfs);
		enhancer.setSuperclass(implClass);
		return enhancer.create();
	}

	private static Callback createCallBack(final Object instance, final Map<Method, List<Interceptor>> interceptors) {
		return new CGLIBInvocationHandler(instance, interceptors);
	}

}
