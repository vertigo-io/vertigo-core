package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XPropertyEntry;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

public final class XDefinitionBody {
	private final List<XDefinitionEntry> definitionEntries;
	private final List<XPropertyEntry> propertyEntries;

	XDefinitionBody(final List<XDefinitionEntry> definitionEntries, final List<XPropertyEntry> propertyEntries) {
		Assertion.checkNotNull(definitionEntries);
		Assertion.checkNotNull(propertyEntries);
		//---------------------------------------------------------------------
		this.definitionEntries = definitionEntries;
		this.propertyEntries = propertyEntries;
	}

	public List<XPropertyEntry> getPropertyEntries() {
		return propertyEntries;
	}

	public List<XDefinitionEntry> getDefinitionEntries() {
		return definitionEntries;
	}
}
