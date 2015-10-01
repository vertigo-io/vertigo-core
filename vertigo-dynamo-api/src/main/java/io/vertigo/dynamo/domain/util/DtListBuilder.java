package io.vertigo.dynamo.domain.util;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * @author pchretien
 */
public final class DtListBuilder<X extends DtObject> implements Builder<DtList<X>> {
	private final DtList<X> list;

	public DtListBuilder(final Class<X> dtObjectClass) {
		Assertion.checkNotNull(dtObjectClass);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtObjectClass);
		list = new DtList(dtDefinition);
	}

	public DtListBuilder(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		list = new DtList(dtDefinition);
	}

	public DtListBuilder<X> add(final X value) {
		Assertion.checkNotNull(value);
		//-----
		list.add(value);
		return this;
	}

	public DtListBuilder<X> addAll(final DtList<X> values) {
		Assertion.checkNotNull(values);
		//-----
		list.addAll(values);
		return this;
	}

	//	public DtListBuilder<X> unmodifiable() {
	//		this.list = Collections.unmodifiableList(list);
	//		return this;
	//	}

	@Override
	public DtList<X> build() {
		return list;
	}
}
