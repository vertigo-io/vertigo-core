package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

/**
 * Identifiant d'un objet powerAMC.
 *
 * @author pchretien
 */
final class OOMId {
	private final String keyValue;

	/**
	 * Constructeur.
	 * @param keyValue Valeur de l'identiant
	 */
	OOMId(final String keyValue) {
		Assertion.checkNotNull(keyValue);
		//------------------------------------------------------------------
		this.keyValue = keyValue;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return keyValue.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof OOMId) {
			return ((OOMId) o).keyValue.equals(this.keyValue);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "id(" + keyValue + ')';
	}
}
