package io.vertigo.dynamo.collections.facet.model;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

import java.io.Serializable;

/**
 * Valeur de facette relatif à une définition.
 * Les valeurs sont 
 *  - soit déclarée.  
 *  - soit déduite.
 * Exemple : 
 *  - Fourchettes de prix (valeurs déclarées)
 *  - Fourchettes de dates (valeurs déclarées)  
 *  - Termes les plus usités (valeurs déduites)
 *  - Clustering sémantique (valeurs déduites) 
 * Fait partie du métamodèle lorsque la facette est déclarée par ses ranges.
 * Fait partie du modèle lorsque les valeurs sont déduites. 
 * 
 * @author pchretien
 * @version $Id: FacetValue.java,v 1.3 2013/10/22 12:24:51 pchretien Exp $ 
 */
public final class FacetValue implements Serializable {
	private static final long serialVersionUID = -7077655936787603783L;
	private final MessageText label;
	private final ListFilter listFilter;

	/**
	 * Contructeur.
	 * @param listFilter Requete pour ce range
	 * @param label Label de cette facette
	 */
	public FacetValue(final ListFilter listFilter, final MessageText label) {
		Assertion.checkNotNull(listFilter);
		Assertion.checkNotNull(label);
		//---------------------------------------------------------------------
		this.listFilter = listFilter;
		this.label = label;
	}

	/**
	 * @return Label de la facette (exemples '1-2 ans' , '3-4 ans', '> 5 ans').
	 */
	public MessageText getLabel() {
		return label;
	}

	/**
	 * @return Méthode de filtrage de la liste.
	 */
	public ListFilter getListFilter() {
		return listFilter;
	}
}
