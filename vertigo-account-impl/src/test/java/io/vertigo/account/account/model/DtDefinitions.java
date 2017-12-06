package io.vertigo.account.account.model;

import java.util.Arrays;
import java.util.Iterator;

public final class DtDefinitions implements Iterable<Class<?>> {
	@Override
	public Iterator<Class<?>> iterator() {
		return Arrays.asList(new Class<?>[] {
				User.class,
				UserGroup.class,
		}).iterator();
	}
}
