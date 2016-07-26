package io.vertigo.dynamo.environment.multi.data;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.dynamo.collections.data.domain.Item;

public final class DtDefinitions implements Iterable<Class<?>> {
	@Override
	public Iterator<Class<?>> iterator() {
		return Arrays.asList(new Class<?>[] {
				Item.class,
		}).iterator();
	}
}
