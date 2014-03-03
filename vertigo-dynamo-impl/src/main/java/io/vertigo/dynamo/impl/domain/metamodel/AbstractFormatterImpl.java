package io.vertigo.dynamo.impl.domain.metamodel;

import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation standard des formatters.
 * Un formatter est un objet partagé, par nature il est non modifiable.
 *
 * @author pchretien
 * @version $Id: AbstractFormatterImpl.java,v 1.3 2013/10/22 10:45:21 pchretien Exp $
 */
public abstract class AbstractFormatterImpl implements Formatter {
	/**
	 * Nom du formatteur.
	 */
	private final String name;

	/**
	 * Constructeur.
	 */
	protected AbstractFormatterImpl(final String name) {
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		this.name = name;
	}

	/**
	 * Initialisation du Formatter par des arguments passés en chaine de caractères.
	 * @param args Paramétrage du Formatter
	 */
	public abstract void initParameters(String args);

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
