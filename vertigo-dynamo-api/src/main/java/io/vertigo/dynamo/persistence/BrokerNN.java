package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;

import java.util.List;

/**
 * Gestion des relations NN.
 *
 * @author  dchallas
 * @version $Id: BrokerNN.java,v 1.4 2014/01/20 17:49:32 pchretien Exp $
 */
public interface BrokerNN {
	/**
	 * Ajout un objet à la collection existante.
	 * @param dtListURI DtList de référence
	 * @param uriToAppend URI de l'objet à ajout à la NN
	 */
	void appendNN(final DtListURIForAssociation dtListURI, final URI<DtObject> uriToAppend);

	/**
	 * Mise à jour des associations n-n. Annule et remplace.
	 * @param dtListURI DtList de référence
	 * @param newUriList  newUriList
	 */
	void updateNN(final DtListURIForAssociation dtListURI, final List<URI<? extends DtObject>> newUriList);

	/**
	 * Supprime toutes les relations liés à l'objet.
	 * @param dtListURI DtList de référence
	 */
	void removeAllNN(final DtListURIForAssociation dtListURI);

	/**
	 * Supprime la relation liés aux deux objets.
	 * Lance une erreur si pas de relation 
	 * @param dtListURI DtList de référence
	 * @param uriToDelete URI de l'objet à supprimer de la NN
	 */
	void removeNN(final DtListURIForAssociation dtListURI, final URI<DtObject> uriToDelete);
}
