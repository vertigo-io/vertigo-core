package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Option;

/**
 * Interface d'objet métier.
 *
 * @author  fconstantin, pchretien
 */
public interface DtObjectReference<D extends DtObject> {
	D getDtObject();

	DtDefinition getDtDefinition();

	//boolean hasPrimaryKey();

	Option<DtField> getPrimaryKey();
	//
	//	Object getId();
}
