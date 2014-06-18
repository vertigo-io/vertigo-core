package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.model.DtObject;

import java.io.Serializable;

/**
 * Critère générique.
 *
 * @author pchretien
 * @param <D> type de l'objet
 */
public interface Criteria<D extends DtObject> extends Serializable {
	//Définir les méthodes
}
