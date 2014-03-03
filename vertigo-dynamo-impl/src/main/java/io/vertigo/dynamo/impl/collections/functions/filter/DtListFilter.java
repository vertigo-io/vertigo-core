package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Définit un filtre à appliquer sur une liste.
 *
 * @author pchretien
 * @version $Id: DtListFilter.java,v 1.2 2014/01/20 17:45:43 pchretien Exp $
 * @param <D> Type du DtObject
 */
public interface DtListFilter<D extends DtObject> {

	/**
	 * Détermine si la ligne considérée doit être acceptée dans la sous-liste.
	 * @param dto Ligne à accepter
	 * @return Si le DTO est accepté
	 */
	boolean accept(D dto);
}
