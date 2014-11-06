package io.vertigo.util;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author pchretien
 */
public final class ListBuilder<X> implements Builder<List<X>> {
	private List<X> list = new ArrayList<>();

	public ListBuilder<X> add(final X value) {
		Assertion.checkNotNull(value);
		//---------------------------------------------------------------------
		list.add(value);
		return this;
	}

	public ListBuilder<X> unmodifiable() {
		this.list = Collections.unmodifiableList(list);
		return this;
	}

	public List<X> build() {
		return list;
	}
}
