package io.vertigo.persona.security.metamodel;

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Security operation's name.
 * @param <D> the dtobject associated to this operation
 * @author npiedeloup
 */
public interface OperationName<D extends DtObject> extends Serializable {

	/** @return nom du champ (const case) */
	String name();

}
