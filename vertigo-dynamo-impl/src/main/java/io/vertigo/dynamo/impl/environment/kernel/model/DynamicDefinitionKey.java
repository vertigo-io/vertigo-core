package io.vertigo.dynamo.impl.environment.kernel.model;

import io.vertigo.kernel.lang.Assertion;

/**
 * Clé d'une definition.
 * @author  pchretien
 */
public final class DynamicDefinitionKey {
	/**
	 * Nom de la dynamic Definition.
	 */
	private final String name;

	/**
	* Constructeur.
	* @param name Nom de la Définition
	*/
	public DynamicDefinitionKey(final String name) {
		Assertion.checkNotNull(name);
		//---------------------------------------------------------------------
		this.name = name;
	}

	/**
	 * @return Nom de la Définition
	 */
	public String getName() {
		return name;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return getName();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof DynamicDefinitionKey) {
			return ((DynamicDefinitionKey) o).getName().equals(getName());
		}
		return false;
	}
}
