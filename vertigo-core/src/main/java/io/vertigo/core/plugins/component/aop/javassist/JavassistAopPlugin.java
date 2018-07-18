/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.plugins.component.aop.javassist;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.vertigo.core.component.AopPlugin;
import io.vertigo.core.component.Component;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ClassUtil;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

/**
 * This class implements the aspects using the javassist library.
 * @author pchretien
 */
public final class JavassistAopPlugin implements AopPlugin {

	/** {@inheritDoc} */
	@Override
	public <C extends Component> C wrap(final C instance, final Map<Method, List<Aspect>> joinPoints) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(joinPoints);
		//check : witgh cglib all methods have to bo non-final
		for (final Method method : joinPoints.keySet()) {
			Assertion.checkArgument(!Modifier.isFinal(method.getModifiers()), "due to cglib method '" + method.getName() + "' on '" + instance.getClass().getName() + "' can not be markedf as final");
		}
		//-----
		final Class<? extends Component> implClass = instance.getClass();
		final Class[] intfs = ClassUtil.getAllInterfaces(implClass).toArray(new Class[0]);

		ProxyFactory f = new ProxyFactory();
		ProxyFactory.onlyPublicMethods = Boolean.TRUE;
		f.setInterfaces(intfs);
		f.setSuperclass(implClass);

		Class c = f.createClass();
		C proxyInstance;
		try {
			proxyInstance = (C) c.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw WrappedException.wrap(e);
		}
		((Proxy) proxyInstance).setHandler(createCallBack(instance, joinPoints));

		return proxyInstance;
	}

	private static JavassistInvocationHandler createCallBack(final Object instance, final Map<Method, List<Aspect>> joinPoints) {
		return new JavassistInvocationHandler(instance, joinPoints);
	}

	@Override
	public <C extends Component> C unwrap(final C component) {
		if (isWrapped(component)) {
			return (C) Stream.of(ProxyFactory.getHandler((Proxy) component))
					.filter(callback -> JavassistInvocationHandler.class.isAssignableFrom(callback.getClass()))
					.map(ourCallBack -> (JavassistInvocationHandler) ourCallBack)
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("the component " + component.getClass() + " is not unwrappable"))
					// we return the unwrapped instance
					.getInstance();
		}
		return component;
	}

	private static boolean isWrapped(final Object object) {
		Assertion.checkNotNull(object);
		// ---
		return ProxyFactory.isProxyClass(object.getClass());
	}

}
