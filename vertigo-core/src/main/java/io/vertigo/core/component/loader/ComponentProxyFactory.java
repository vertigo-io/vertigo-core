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
import io.vertigo.lang.Tuples;
import io.vertigo.lang.Tuples.Tuple2;

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
		private final Map<Method, Tuple2<ProxyMethod, Annotation>> tuples;
		private final Map<Method, List<Aspect>> joinPoints;

		MyInvocationHandler(
				final Class<? extends Component> intf,
				final List<ProxyMethod> proxyMethods,
				final Map<Method, List<Aspect>> joinPoints) {
			Assertion.checkNotNull(proxyMethods);
			//---
			tuples = Arrays.stream(intf.getDeclaredMethods())
					.collect(Collectors.toMap(method -> method,
							method -> findTuple(method, proxyMethods)));
			this.joinPoints = joinPoints;
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
					joinPoints.get(method),
					tuples.get(method))
							.proceed(args);
		}
	}

	private static Tuples.Tuple2<ProxyMethod, Annotation> findTuple(
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

		return Tuples.of(proxyMethod, annotation);
	}

	private static final class MyMethodInvocation implements AspectMethodInvocation {
		private final List<Aspect> aspects;
		private final Method method;
		private int index;
		private final Tuple2<ProxyMethod, Annotation> tuple;

		private MyMethodInvocation(
				final Method method,
				final List<Aspect> aspects,
				final Tuple2<ProxyMethod, Annotation> tuple) {
			Assertion.checkNotNull(method);
			Assertion.checkNotNull(aspects);
			Assertion.checkNotNull(tuple);
			//-----
			this.method = method;
			this.aspects = aspects;
			this.tuple = tuple;
		}

		/** {@inheritDoc} */
		@Override
		public Object proceed(final Object[] args) {
			if (index < aspects.size()) {
				return aspects.get(index++).invoke(args, this);
			}
			return tuple.getVal1().invoke(tuple.getVal2(), method, args);
		}

		@Override
		public Method getMethod() {
			return getMethod();
		}
	}
}
