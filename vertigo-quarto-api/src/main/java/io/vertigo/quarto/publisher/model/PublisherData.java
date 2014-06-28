package io.vertigo.quarto.publisher.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.publisher.metamodel.PublisherDataDefinition;

/**
 * Donn�es � fusionner.
 *
 * @author npiedeloup
 * @version $Id: PublisherData.java,v 1.3 2013/10/22 12:07:11 pchretien Exp $
 */
public final class PublisherData {
	private final PublisherDataDefinition publisherDataDefinition;
	private final PublisherNode root;

	/**
	 * Constructeur.
	 * @param dataDefinition Definition des donn�es de publication
	 */
	public PublisherData(final PublisherDataDefinition dataDefinition) {
		Assertion.checkNotNull(dataDefinition);
		//---------------------------------------------------------------------
		publisherDataDefinition = dataDefinition;
		root = new PublisherNode(dataDefinition.getRootNodeDefinition());
	}

	/**
	 * @return D�finition des donn�es.
	 */
	public PublisherDataDefinition getDefinition() {
		return publisherDataDefinition;
	}

	/**
	 * @return Noeud racine
	 */
	public PublisherNode getRootNode() {
		return root;
	}
}
