package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Option;

/**
 * Interface d'objet métier.
 *
 * @author  fconstantin, pchretien
 * @version $Id: DtObjectReference.java,v 1.3 2013/10/22 10:42:45 pchretien Exp $
 */
public interface DtObjectReference<D extends DtObject> {
	D getDtObject();

	DtDefinition getDtDefinition();

	//boolean hasPrimaryKey();

	Option<DtField> getPrimaryKey();
	//
	//	Object getId();
}
