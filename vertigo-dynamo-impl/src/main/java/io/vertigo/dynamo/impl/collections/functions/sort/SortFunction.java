package io.vertigo.dynamo.impl.collections.functions.sort;

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fonction de tri.
 * @author pchretien
 */
public final class SortFunction<D extends DtObject> implements DtListFunction<D> {
	private final SortState sortState;
	private final PersistenceManager persistenceManager;

	public SortFunction(final SortState sortState, final PersistenceManager persistenceManager) {
		Assertion.checkNotNull(persistenceManager);
		Assertion.checkNotNull(sortState);
		//-----------------------------------------------------------------
		this.sortState = sortState;
		this.persistenceManager = persistenceManager;
	}

	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		Assertion.checkNotNull(sortState);
		//----------------------------------------------------------------------
		//On crée une liste triable par l'utilitaire java.util.Collections
		final List<D> list = new ArrayList<>(dtc);

		//On trie. 
		final Comparator<D> comparator = new DtObjectComparator<>(persistenceManager, dtc, sortState);
		Collections.sort(list, comparator);

		//On reconstitue la collection.
		final DtList<D> sortedDtc = new DtList<>(dtc.getDefinition());
		sortedDtc.addAll(list);
		return sortedDtc;
	}
}
