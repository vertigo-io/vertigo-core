package io.vertigo.dynamo.impl.domain.metamodel;

import io.vertigo.dynamo.domain.metamodel.Constraint;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

/**
 * Par nature une contrainte est une ressource partagée et non modifiable.
 *
 * @author pchretien
 * @version $Id: AbstractConstraintImpl.java,v 1.4 2013/10/22 10:45:21 pchretien Exp $
 * @param <J> Type java de la propriété associée à la contrainte
 * @param <D> Type java de la valeur à contréler
 */
public abstract class AbstractConstraintImpl<J, D> implements Constraint<J, D> {
	/**
	 * Nom de la contrainte.
	 * On n'utilise pas les génériques car problémes.
	 */
	private final String name;

	/**
	 * Message d'erreur.
	 */
	private MessageText msg;

	/**
	 * Constructeur.
	 */
	protected AbstractConstraintImpl(final String name) {
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		this.name = name;
	}

	/**
	 * Initialisation de la contrainte par des arguments passés en chaine de caractères.
	 * @param args Paramétrage de la contrainte
	 */
	public abstract void initParameters(String args);

	/**
	 * Initialisation du message d'erreur si celui-ci est précisé de façon externe.
	 * @param newMsg Message d'erreur (Nullable)
	 */
	public final void initMsg(final MessageText newMsg) {
		this.msg = newMsg;
	}

	/**
	 * @return Message d'erreur (Nullable)
	 */
	public final MessageText getErrorMessage() {
		return msg != null ? msg : getDefaultMessage();
	}

	/**
	 * @return Message par défaut
	 */
	protected abstract MessageText getDefaultMessage();

	/** {@inheritDoc} */
	public final String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}
}
