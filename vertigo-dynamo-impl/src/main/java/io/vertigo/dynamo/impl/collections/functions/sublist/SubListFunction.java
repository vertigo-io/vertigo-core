package io.vertigo.dynamo.impl.collections.functions.sublist;

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

/**
 * Fonction de sous-liste.
 * @author pchretien
 * @version $Id: SubListFunction.java,v 1.5 2014/01/20 17:46:01 pchretien Exp $
 */
public final class SubListFunction<D extends DtObject> implements DtListFunction<D> {
	private final int start, end;

	public SubListFunction(final int start, final int end) {
		this.start = start;
		this.end = end;
	}

	/** {@inheritDoc} */
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		Assertion.checkArgument(start >= 0 && end <= dtc.size() && start <= end, "IndexOutOfBoundException, le subList n''est pas possible avec les index passés (start:{0}, end:{1}, size:{2})", String.valueOf(start), String.valueOf(end), String.valueOf(dtc.size())); //condition tirée de la javadoc de subList sur java.util.List
		//----------------------------------------------------------------------
		final DtList<D> subDtc = new DtList<>(dtc.getDefinition());
		for (int i = start; i < end; i++) {
			subDtc.add(dtc.get(i));
		}
		return subDtc;
	}
}
