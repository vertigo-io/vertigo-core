package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.model.DtObject;

import java.io.Serializable;

/**
 * Critère générique.
 *
 * @author pchretien
 * @version $Id: Criteria.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 * @param <D> type de l'objet
 */
public interface Criteria<D extends DtObject> extends Serializable {
	//Définir les méthodes
}
