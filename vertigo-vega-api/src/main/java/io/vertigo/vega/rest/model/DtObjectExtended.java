package io.vertigo.vega.rest.model;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;

/**
 * UiObjectExtended to extends an dtObject with meta data.
 */
public final class DtObjectExtended<D extends DtObject> extends HashMap<String, Serializable> {
	private static final long serialVersionUID = -8118714236186836600L;

	private final D innerObject;

	/**
	 * Constructor.
	 * @param dtObject inner object
	 */
	public DtObjectExtended(final D dtObject) {
		Assertion.checkNotNull(dtObject);
		//---------------------------------------------------------------------
		this.innerObject = dtObject;
	}

	/**
	 * @return Inner object
	 */
	public D getInnerObject() {
		return innerObject;
	}
}
