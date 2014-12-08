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
import io.vertigo.core.aop.AspectMethodInvocation;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author pchretien
 */
final class CGLIBInvocationHandler implements net.sf.cglib.proxy.InvocationHandler {
	private final Object instance;
	private final Map<Method, List<Aspect>> joinPoints;

	CGLIBInvocationHandler(final Object instance, final Map<Method, List<Aspect>> joinPoints) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(joinPoints);
		//-----------------------------------------------------------------
		this.instance = instance;
		this.joinPoints = joinPoints;
	}

	/** {@inheritDoc} */
	@Override
	public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (!joinPoints.containsKey(method)) {
			//Si pas d'intercepteur sur la méthode. 
			return ClassUtil.invoke(instance, method, args);
		}
		return new MyMethodInvocation(instance, method, joinPoints.get(method)).proceed(args);
	}

	private static final class MyMethodInvocation implements AspectMethodInvocation {
		private final List<Aspect> interceptors;
		private final Object instance;
		private final Method method;
		private int index = 0;

		private MyMethodInvocation(final Object instance, final Method method, final List<Aspect> interceptors) {
			Assertion.checkNotNull(instance);
			Assertion.checkNotNull(method);
			Assertion.checkNotNull(interceptors);
			//-----------------------------------------------------------------
			this.instance = instance;
			this.method = method;
			this.interceptors = interceptors;
		}

		/** {@inheritDoc} */
		@Override
		public Object proceed(final Object[] args) throws Throwable {
			if (index < interceptors.size()) {
				return interceptors.get(index++).invoke(args, this);
			}
			return ClassUtil.invoke(instance, method, args);
		}

		/** {@inheritDoc} */
		@Override
		public Method getMethod() {
			return method;
		}
	}
}
