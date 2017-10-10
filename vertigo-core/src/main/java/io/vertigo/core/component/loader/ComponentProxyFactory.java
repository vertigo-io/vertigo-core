package io.vertigo.core.component.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.core.component.Component;
import io.vertigo.core.component.aop.Aspect;
import io.vertigo.core.component.aop.AspectMethodInvocation;
import io.vertigo.core.component.proxy.ProxyMethod;
import io.vertigo.core.component.proxy.ProxyMethodAnnotation;
import io.vertigo.lang.Assertion;

final class ComponentProxyFactory {
	static <C extends Component> C createProxy(
			final Class<C> intf,
			final List<ProxyMethod> proxyMethods,
			final Map<Method, List<Aspect>> joinPoints) {
		Assertion.checkNotNull(intf);
		Assertion.checkArgument(intf.isInterface(), "only interface can be proxified");
		Assertion.checkNotNull(proxyMethods);
		Assertion.checkNotNull(joinPoints);
		//---
		final InvocationHandler handler = new MyInvocationHandler(intf, proxyMethods, joinPoints);
		return (C) java.lang.reflect.Proxy.newProxyInstance(
				intf.getClassLoader(),
				new Class[] { intf },
				handler);
	}

	private static final class MyInvocationHandler implements InvocationHandler {
		private final Map<Method, ProxyMethod> proxyMethodsByMethod;
		private final Map<Method, List<Aspect>> aspectsByMethod;

		MyInvocationHandler(
				final Class<? extends Component> intf,
				final List<ProxyMethod> proxyMethods,
				final Map<Method, List<Aspect>> aspectsByMethod) {
			Assertion.checkNotNull(proxyMethods);
			//---
			proxyMethodsByMethod = Arrays.stream(intf.getDeclaredMethods())
					.collect(Collectors.toMap(method -> method,
							method -> findProxyMethod(method, proxyMethods)));
			this.aspectsByMethod = aspectsByMethod;
		}

		@Override
		public Object invoke(
				final Object instance,
				final Method method,
				final Object[] args) throws Throwable {
			Assertion.checkNotNull(instance);
			Assertion.checkNotNull(method);
			//---
			return new MyMethodInvocation(method,
					aspectsByMethod.get(method),
					proxyMethodsByMethod.get(method))
							.proceed(args);
		}
	}

	private static ProxyMethod findProxyMethod(
			final Method method,
			final List<ProxyMethod> proxyMethods) {
		final Annotation annotation = Arrays.stream(method.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(ProxyMethodAnnotation.class))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No way to find a proxy annotaion on method : " + method));

		final ProxyMethod proxyMethod = proxyMethods
				.stream()
				.filter(p -> annotation.annotationType().equals(p.getAnnotationType()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No way to find a proxy annotation on method : " + method));

		return proxyMethod;
	}

	private static final class MyMethodInvocation implements AspectMethodInvocation {
		private final List<Aspect> aspects;
		private final Method method;
		private int index;
		private final ProxyMethod proxyMethod;

		private MyMethodInvocation(
				final Method method,
				final List<Aspect> aspects,
				final ProxyMethod proxyMethod) {
			Assertion.checkNotNull(method);
			Assertion.checkNotNull(aspects);
			Assertion.checkNotNull(proxyMethod);
			//-----
			this.method = method;
			this.aspects = aspects;
			this.proxyMethod = proxyMethod;
		}

		/** {@inheritDoc} */
		@Override
		public Object proceed(final Object[] args) {
			if (index < aspects.size()) {
				return aspects.get(index++).invoke(args, this);
			}
			return proxyMethod.invoke(method, args);
		}

		@Override
		public Method getMethod() {
			return method;
		}
	}
}
