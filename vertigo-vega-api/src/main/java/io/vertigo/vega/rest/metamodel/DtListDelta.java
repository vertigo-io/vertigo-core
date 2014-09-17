package io.vertigo.vega.rest.metamodel;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Delta operations on List.
 * @author npiedeloup (16 sept. 2014 18:13:55)
 * @param <D> Object type
 */
public final class DtListDelta<D extends DtObject> {
	private final DtList<D> dtListCreates;
	private final DtList<D> dtListUpdates;
	private final DtList<D> dtListDeletes;

	public DtListDelta(final DtList<D> dtListCreates, final DtList<D> dtListUpdates, final DtList<D> dtListDeletes) {
		this.dtListCreates = dtListCreates;
		this.dtListUpdates = dtListUpdates;
		this.dtListDeletes = dtListDeletes;
	}

	/**
	 * @return Created objects.
	 */
	public DtList<D> getCreated() {
		return dtListCreates;
	}

	/**
	 * @return Updated objects.
	 */
	public DtList<D> getUpdated() {
		return dtListUpdates;
	}

	/**
	 * @return Deleted objects.
	 */
	public DtList<D> getDeleted() {
		return dtListDeletes;
	}
}
