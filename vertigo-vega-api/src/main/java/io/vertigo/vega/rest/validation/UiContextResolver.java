package io.vertigo.vega.rest.validation;

import io.vertigo.dynamo.domain.model.DtObject;

import java.util.HashMap;
import java.util.Map;

/**
* @author npiedeloup 
*/
public final class UiContextResolver {
	private final Map<DtObject, String> dtObjectDictionary = new HashMap<>();

	public void register(final String contextKey, final DtObject dtObject) {
		dtObjectDictionary.put(dtObject, contextKey);
	}

	public String resolveContextKey(final DtObject dtObject) {
		return dtObjectDictionary.get(dtObject);
	}

}
