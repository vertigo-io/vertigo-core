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
import io.vertigo.kernel.aop.MethodInvocation;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


/**
 * @author pchretien
 */
final class CGLIBInvocationHandler implements net.sf.cglib.proxy.InvocationHandler {
	private final Object instance;
	private final Map<Method, List<Interceptor>> interceptors;

	CGLIBInvocationHandler(final Object instance, final Map<Method, List<Interceptor>> interceptors) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(interceptors);
		//-----------------------------------------------------------------
		this.instance = instance;
		this.interceptors = interceptors;
	}

	/** {@inheritDoc} */
	public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (!interceptors.containsKey(method)) {
			//Si pas d'intercepteur sur la méthode. 
			return ClassUtil.invoke(instance, method, args);
		}
		return new MyMethodInvocation(instance, method, interceptors.get(method)).proceed(args);
	}

	private static final class MyMethodInvocation implements MethodInvocation {
		private final List<Interceptor> interceptors;
		private final Object instance;
		private final Method method;
		private int index = 0;

		private MyMethodInvocation(final Object instance, final Method method, final List<Interceptor> interceptors) {
			Assertion.checkNotNull(instance);
			Assertion.checkNotNull(method);
			Assertion.checkNotNull(interceptors);
			//-----------------------------------------------------------------
			this.instance = instance;
			this.method = method;
			this.interceptors = interceptors;
		}

		/** {@inheritDoc} */
		public Object proceed(final Object[] args) throws Throwable {
			if (index < interceptors.size()) {
				return interceptors.get(index++).invoke(args, this);
			}
			return ClassUtil.invoke(instance, method, args);
		}

		/** {@inheritDoc} */
		public Method getMethod() {
			return method;
		}
	}
}
