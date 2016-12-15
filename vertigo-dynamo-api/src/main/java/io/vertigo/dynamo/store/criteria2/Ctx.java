package io.vertigo.dynamo.store.criteria2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;

public final class Ctx {
	private int i;
	private final Map<String, Object> attributeValues = new HashMap<>();
	private final Map<String, DtFieldName> attributeNames = new HashMap<>();

	String attributeName(final DtFieldName dtFieldName, final Object value) {
		final String attributeName = dtFieldName.name() + '_' + i;
		i++;
		attributeValues.put(attributeName, value);
		attributeNames.put(attributeName, dtFieldName);
		return attributeName;
	}

	public Set<String> getAttributeNames() {
		return attributeValues.keySet();
	}

	public DtFieldName getDtFieldName(final String attributeName) {
		return attributeNames.get(attributeName);
	}

	public Object getAttributeValue(final String attributeName) {
		return attributeValues.get(attributeName);
	}
}
