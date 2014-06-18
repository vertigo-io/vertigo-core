package io.vertigo.dynamo.transaction;

import io.vertigo.kernel.lang.Assertion;

/**
 * Identification des ressources participant à la transaction.
 *
 * @author  pchretien
 * @param <TR> Ressource transactionnelle.
 */
public final class KTransactionResourceId<TR extends KTransactionResource> {
	/**
	 * Ordre dans lequel les ressources sont commitées.
	 * @author pchretien
	 */
	public static enum Priority {
		/**
		 * Priorité maximale.
		 * Doit être utilisée pour la ressource critique.
		 */
		TOP,
		/**
		 * Priorité normale.
		 */
		NORMAL,
		/**
		 * Priorité faible.
		 * Doit être utilisée pour les ressources pouvant -facilement- dysfonctionner.
		 */
		LOW,
	}

	private final Priority priority;
	private final String name;

	/**
	 * Constructeur.
	 * @param priority Priorité de la ressource.
	 * @param name Nom de code de la ressource.
	 */
	public KTransactionResourceId(final Priority priority, final String name) {
		Assertion.checkNotNull(priority);
		Assertion.checkNotNull(name);
		//---------------------------------------------------------------------
		this.priority = priority;
		this.name = name;
	}

	/**
	 * @return Priorité de la ressource.
	 */
	public Priority getPriority() {
		return priority;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object object) {
		if (object instanceof KTransactionResourceId<?>) {
			return name.equals(((KTransactionResourceId<?>) object).name);
		}
		return false;
	}
}
