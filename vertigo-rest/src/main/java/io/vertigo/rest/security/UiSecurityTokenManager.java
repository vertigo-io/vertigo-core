package io.vertigo.rest.security;

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.component.Manager;

/**
 * Manager of Security Access Token.
 * @author npiedeloup (16 juil. 2014 12:49:49)
 */
public interface UiSecurityTokenManager extends Manager {

	/**
	 * Store object and return unique key.
	 * @param data Object to store
	 * @return unique key of this object
	 */
	 String put(Serializable data);

	/**
	 * Get object by key.
	 * @param key key of this object
	 * @return Object store or null if unknown
	 */
	Serializable get(String key);

	/**
	 * Get and remove object by key.
	 * @param key key of this object
	 * @return Object store or null if unknown
	 */
	Serializable getAndRemove(String key);

}
