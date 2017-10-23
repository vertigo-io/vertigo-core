package io.vertigo.core.component.proxy.data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import io.vertigo.core.component.proxy.ProxyMethod;

public final class AggregatorProxyMethod implements ProxyMethod {

	@Override
	public Class<AggregatorAnnotation> getAnnotationType() {
		return AggregatorAnnotation.class;
	}

	@Override
	public Object invoke(final Method method, final Object[] args) {
		final AggregatorAnnotation aggregatorAnnotation = method.getAnnotation(AggregatorAnnotation.class);

		final Stream<Integer> stream = Arrays.stream(args)
				.map(arg -> Integer.class.cast(arg));

		switch (aggregatorAnnotation.operation()) {
			case max:
				return stream.max(Integer::compare).orElse(null);
			case min:
				return stream.min(Integer::compare).orElse(null);
			case count:
				return stream.count();
			default:
				throw new IllegalStateException();
		}
	}
}
