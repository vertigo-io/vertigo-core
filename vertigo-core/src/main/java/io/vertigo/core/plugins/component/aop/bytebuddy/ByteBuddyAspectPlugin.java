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
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.AspectPlugin;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.aspect.Aspect;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * This class implements the aspects using the ByteBuddy library.
 * Unlike the Javassist implementation, ByteBuddy creates subclass proxies
 * that do NOT re-implement interfaces, making it compatible with sealed interfaces.
 *
 * @author pchretien
 */
public final class ByteBuddyAspectPlugin implements AspectPlugin {
	private static final String HANDLER_FIELD_NAME = "vertigo$handler";

	/** {@inheritDoc} */
	@Override
	public <C extends CoreComponent> C wrap(final C instance, final Map<Method, List<Aspect>> joinPoints) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(joinPoints);
		//check : all methods have to be non-final to be overridden by the proxy
		for (final Method method : joinPoints.keySet()) {
			Assertion.check()
					.isFalse(Modifier.isFinal(method.getModifiers()), "method '" + method.getName() + "' on '" + instance.getClass().getName() + "' can not be marked as final");
		}
		//-----
		final Class<? extends CoreComponent> implClass = instance.getClass();
		//check : the proxy subclass mirrors the component's constructors and is instantiated through its no-arg constructor
		try {
			implClass.getDeclaredConstructor();
		} catch (final NoSuchMethodException e) {
			throw new IllegalStateException("class '" + implClass.getName() + "' must declare a no-arg constructor to support aspects (use field injection instead of constructor injection)", e);
		}
		final ByteBuddyInvocationHandler handler = new ByteBuddyInvocationHandler(instance, joinPoints);

		try {
			final Class<? extends CoreComponent> proxyClass = new ByteBuddy()
					.subclass(implClass)
					.defineField(HANDLER_FIELD_NAME, InvocationHandler.class, Modifier.PUBLIC)
					.method(ElementMatchers.any())
					.intercept(InvocationHandlerAdapter.toField(HANDLER_FIELD_NAME))
					.make()
					.load(implClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
					.getLoaded();

			final C proxyInstance = (C) proxyClass.getDeclaredConstructor().newInstance();
			proxyClass.getField(HANDLER_FIELD_NAME).set(proxyInstance, handler);
			return proxyInstance;
		} catch (final Exception e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public <C extends CoreComponent> C unwrap(final C component) {
		if (isWrapped(component)) {
			return (C) getHandler(component).getInstance();
		}
		return component;
	}

	private static boolean isWrapped(final Object object) {
		Assertion.check()
				.isNotNull(object);
		// ---
		try {
			object.getClass().getField(HANDLER_FIELD_NAME);
			return true;
		} catch (final NoSuchFieldException e) {
			return false;
		}
	}

	private static ByteBuddyInvocationHandler getHandler(final Object proxy) {
		try {
			final InvocationHandler handler = (InvocationHandler) proxy.getClass().getField(HANDLER_FIELD_NAME).get(proxy);
			if (handler instanceof final ByteBuddyInvocationHandler byteBuddyHandler) {
				return byteBuddyHandler;
			}
			throw new IllegalArgumentException("the component " + proxy.getClass() + " is not unwrappable");
		} catch (final NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalArgumentException("the component " + proxy.getClass() + " is not unwrappable", e);
		}
	}
}
