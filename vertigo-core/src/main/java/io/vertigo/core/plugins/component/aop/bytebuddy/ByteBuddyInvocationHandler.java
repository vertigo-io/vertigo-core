/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.plugins.component.aop.bytebuddy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.aspect.Aspect;
import io.vertigo.core.node.component.aspect.AspectMethodInvocation;
import io.vertigo.core.util.ClassUtil;

/**
 * Invocation handler for ByteBuddy-based AOP proxies.
 * Delegates method calls to the original instance, applying aspects when defined.
 *
 * @author pchretien
 */
final class ByteBuddyInvocationHandler implements InvocationHandler {
	private final Object instance;
	private final Map<Method, List<Aspect>> joinPoints;

	ByteBuddyInvocationHandler(final Object instance, final Map<Method, List<Aspect>> joinPoints) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(joinPoints);
		//-----
		this.instance = instance;
		this.joinPoints = joinPoints;
	}

	Object getInstance() {
		return instance;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) {
		//InvocationHandler contract : args is null for no-arg methods, aspects expect an empty array
		final Object[] actualArgs = args != null ? args : new Object[0];
		if (!joinPoints.containsKey(method)) {
			return ClassUtil.invoke(instance, method, actualArgs);
		}
		return new MyMethodInvocation(instance, method, joinPoints.get(method)).proceed(actualArgs);
	}

	private static final class MyMethodInvocation implements AspectMethodInvocation {
		private final List<Aspect> aspects;
		private final Object instance;
		private final Method method;
		private int index;

		private MyMethodInvocation(final Object instance, final Method method, final List<Aspect> aspects) {
			Assertion.check()
					.isNotNull(instance)
					.isNotNull(method)
					.isNotNull(aspects);
			//-----
			this.instance = instance;
			this.method = method;
			this.aspects = aspects;
		}

		@Override
		public Object proceed(final Object[] args) {
			if (index < aspects.size()) {
				return aspects.get(index++).invoke(args, this);
			}
			return ClassUtil.invoke(instance, method, args);
		}

		@Override
		public Method getMethod() {
			return method;
		}
	}
}
