package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import io.vertigo.kernel.lang.Assertion;

/**
 * Classe de gestion des identifiants du XMI.
 * @author pforhan
 *
 */
public class EAXmiId {
	private final String keyValue;

	/**
	 * Constructeur.
	 * @param keyValue Valeur de l'identiant
	 */
	EAXmiId(final String keyValue) {
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
		if (o instanceof EAXmiId) {
			return ((EAXmiId) o).keyValue.equals(this.keyValue);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "id(" + keyValue + ')';
	}

}
