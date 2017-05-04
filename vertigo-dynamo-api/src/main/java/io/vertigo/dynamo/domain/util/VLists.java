package io.vertigo.dynamo.domain.util;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

/**
 * Utils on Lists.
 * @author pchretien
 */
public final class VLists {

	private VLists() {
		super();
	}

	/**
	 * Builds a sub list from a list without changing it.
	 * @param list the list to partition
	 * @param start the start index (Included)
	 * @param end the end index (excluded)
	 * @return the partitioned list
	 */
	public static <D extends DtObject> DtList<D> subList(final DtList<D> list, final int start, final int end) {
		Assertion.checkNotNull(list);
		Assertion.checkArgument(start >= 0 && start <= end && end <= list.size(),
				"IndexOutOfBoundException, can not partition a list sized:'{2}'from start:'{0}' to end:'{1}'",
				String.valueOf(start), String.valueOf(end), String.valueOf(list.size()));
		//same condition as subList on  java.util.List
		//-----
		final DtList<D> subList = new DtList<>(list.getDefinition());
		for (int i = start; i < end; i++) {
			subList.add(list.get(i));
		}
		return subList;
	}
}
