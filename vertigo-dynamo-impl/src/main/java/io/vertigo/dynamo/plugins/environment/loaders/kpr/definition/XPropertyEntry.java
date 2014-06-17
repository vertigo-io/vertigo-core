package io.vertigo.dynamo.plugins.environment.loaders.kpr.definition;

import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.kernel.lang.Assertion;

/**
 * Gestion des couples : propriété et valeur.
 *
 * @author pchretien
 */
public final class XPropertyEntry {
	private final String propertyValue;
	private final EntityProperty property;

	/**
	 * Constructeur.
	 * @param property Propriété
	 * @param propertyValue Valeur de la propriété
	 */
	public XPropertyEntry(final EntityProperty property, final String propertyValue) {
		Assertion.checkNotNull(property);
		//----------------------------------------------------------------------
		this.property = property;
		this.propertyValue = propertyValue;
	}

	/**
	 * @return Valeur de la propriété
	 */
	public String getPropertyValueAsString() {
		return propertyValue;
	}

	/**
	 * @return Propriété
	 */
	public EntityProperty getProperty() {
		return property;
	}
}
