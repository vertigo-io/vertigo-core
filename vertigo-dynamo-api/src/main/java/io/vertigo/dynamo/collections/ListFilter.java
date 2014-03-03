package io.vertigo.dynamo.collections;

import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;


/**
 * Filtre de liste. 
 * Construit ListFilter sous forme de chaine.
 * @author pchretien, npiedeloup
 * @version $Id: ListFilter.java,v 1.3 2013/10/22 12:37:12 pchretien Exp $
 */
public final class ListFilter implements Serializable {
	private static final long serialVersionUID = -4685077662421935642L;

	private final String filterValue;

	/**
	 * Constructeur d'un filtre à partir d'une syntaxe.
	 * Syntaxe acceptée :
	 * FIELD_NAME:VALUE => FilterByValue.
	 * 
	 * FIELD_NAME:[MINVALUE TO MAXVALUE]
	 * - Le min et max doivent être du même type.
	 * - Le caractère * peut être utiliser pour indiquer qu'il n'y a pas de borne max ou min.
	 * - Les accolades sont ouvrantes ou fermantes pour indiquer si la valeur est comprise ou non
	 * 
	 * @param filterValue Valeur du filtre
	 */
	public ListFilter(final String filterValue) {
		Assertion.checkNotNull(filterValue);
		//---------------------------------------------------------------------
		this.filterValue = filterValue;
	}

	/**
	 * @return Valeur du filtre
	 */
	public String getFilterValue() {
		return filterValue;
	}
}
