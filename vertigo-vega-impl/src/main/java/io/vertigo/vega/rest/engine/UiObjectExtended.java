package io.vertigo.vega.rest.engine;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;

/**
 * UiObjectExtended to extends an dtObject with meta data.
 */
public final class UiObjectExtended<D extends DtObject> extends HashMap<String, Serializable> {
	private static final long serialVersionUID = -8118714236186836600L;

	private final UiObject<D> innerObject;

	/**
	 * Constructor.
	 * @param uiObject inner object
	 */
	public UiObjectExtended(final UiObject<D> uiObject) {
		Assertion.checkNotNull(uiObject);
		//---------------------------------------------------------------------
		this.innerObject = uiObject;
	}

	/**
	 * @return Inner object
	 */
	public UiObject<D> getInnerObject() {
		return innerObject;
	}
}
