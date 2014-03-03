package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

/**
 * Fonction de filtre.
 * @author pchretien
 * @version $Id: FilterFunction.java,v 1.4 2014/01/20 17:45:43 pchretien Exp $
 */
public final class FilterFunction<D extends DtObject> implements DtListFunction<D> {
	private final DtListFilter<D> filter;

	public FilterFunction(final DtListFilter<D> filter) {
		Assertion.checkNotNull(filter);
		//-----------------------------------------------------------------
		this.filter = filter;
	}

	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		//----------------------------------------------------------------------
		final DtList<D> filteredDtc = new DtList<>(dtc.getDefinition());
		for (final D dto : dtc) {
			if (filter.accept(dto)) {
				filteredDtc.add(dto);
			}
		}
		return filteredDtc;
	}
}
