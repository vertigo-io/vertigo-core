package io.vertigo.dynamo.collections.data;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.dynamo.collections.data.domain.Car;
import io.vertigo.dynamo.collections.data.domain.Item;

public final class DtDefinitions implements Iterable<Class<?>> {
	@Override
	public Iterator<Class<?>> iterator() {
		return Arrays.asList(new Class<?>[] {
				Item.class,
				Car.class
		}).iterator();
	}
}
