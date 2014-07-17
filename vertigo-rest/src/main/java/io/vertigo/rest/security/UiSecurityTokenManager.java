package io.vertigo.rest.security;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.component.Manager;

/**
 * Manager of Security Access Token.
 * @author npiedeloup (16 juil. 2014 12:49:49)
 */
public interface UiSecurityTokenManager extends Manager {

	/**
	 * Store object and return unique key.
	 * @param <D> Object type
	 * @param data Object to store
	 * @return unique key of this object
	 */
	<D extends DtObject> String put(D data);

	/**
	 * Get object by key.
	 * @param <D> Object type
	 * @param key key of this object
	 * @return Object store or null if unknown
	 */
	<D extends DtObject> D get(String key);

	/**
	 * Get and remove object by key.
	 * @param <D> Object type
	 * @param key key of this object
	 * @return Object store or null if unknown
	 */
	<D extends DtObject> D getAndRemove(String key);

}
