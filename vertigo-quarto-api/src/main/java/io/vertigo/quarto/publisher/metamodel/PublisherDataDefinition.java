package io.vertigo.quarto.publisher.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * D�finition d'un mod�le d'�dition.
 * Un mod�le d'�dition est un arbre de donn�es.
 *
 * @author npiedeloup, pchretien
 * @version $Id: PublisherDataDefinition.java,v 1.3 2013/10/22 10:50:53 pchretien Exp $
 */
@Prefix("PU")
public final class PublisherDataDefinition implements Definition {
	/** Nom de la d�finition. */
	private final String name;
	private final PublisherNodeDefinition rootNodeDefinition;

	public PublisherDataDefinition(final String name, final PublisherNodeDefinition rootNodeDefinition) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(rootNodeDefinition);
		//---------------------------------------------------------------------
		this.name = name;
		this.rootNodeDefinition = rootNodeDefinition;
	}

	/**
	 * @return D�finition du noeud racine
	 */
	public PublisherNodeDefinition getRootNodeDefinition() {
		return rootNodeDefinition;
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
