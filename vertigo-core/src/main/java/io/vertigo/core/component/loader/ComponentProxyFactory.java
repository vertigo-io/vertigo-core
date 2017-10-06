package io.vertigo.core.component.loader;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.core.component.Component;
import io.vertigo.core.component.proxy.ProxyMethod;
import io.vertigo.core.component.proxy.ProxyMethodAnnotation;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;
import io.vertigo.lang.Tuples.Tuple2;

final class ComponentProxyFactory {
	static <C extends Component> C createProxy(final Class<C> intf, final List<ProxyMethod> proxyMethods) {
		Assertion.checkNotNull(intf);
		Assertion.checkArgument(intf.isInterface(), "only interface can be proxified");
		Assertion.checkNotNull(proxyMethods);
		//---
		final InvocationHandler handler = new MyInvocationHandler(intf, proxyMethods);
		return (C) java.lang.reflect.Proxy.newProxyInstance(
				intf.getClassLoader(),
				new Class[] { intf },
				handler);
	}

	private static final class MyInvocationHandler implements InvocationHandler {
		private final Map<Method, Tuple2<ProxyMethod, Annotation>> map;

		public MyInvocationHandler(final Class<? extends Component> intf, final List<ProxyMethod> proxyMethods) {
			Assertion.checkNotNull(proxyMethods);
			//---
			map = Arrays.stream(intf.getDeclaredMethods())
					.collect(Collectors.toMap(method -> method,
							method -> findTuple(method, proxyMethods)));
		}

		@Override
		public Object invoke(final Object instance, final Method method, final Object[] args) throws Throwable {
			Assertion.checkNotNull(method);
			//---
			//We seek all annotations annotated by @ProxyMethodAnnotation
			//There must be one and only one
			final Tuple2<ProxyMethod, Annotation> tuple = map.get(method);
			Assertion.checkNotNull(tuple, "No proxy found on method {0}", method);

			return tuple.getVal1().invoke(tuple.getVal2(), method, args);
		}

	}

	private static Tuples.Tuple2<ProxyMethod, Annotation> findTuple(final Method method, final List<ProxyMethod> proxyMethods) {
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
}
