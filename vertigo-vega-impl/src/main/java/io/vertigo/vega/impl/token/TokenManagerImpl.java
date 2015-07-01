/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.vega.impl.token;

import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.vega.token.TokenManager;

import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Ui data security token.
 * Keep an security token by user and DtObject.
 *
 * @author npiedeloup
 */
public final class TokenManagerImpl implements TokenManager {

	private final String dataStoreName;
	private final VSecurityManager securityManager;
	/** Object token, by */
	private final StoreManager storeManager;

	@Inject
	public TokenManagerImpl(@Named("dataStoreName") final String dataStoreName, final VSecurityManager securityManager, final StoreManager storeManager) {
		Assertion.checkArgNotEmpty(dataStoreName);
		Assertion.checkNotNull(securityManager);
		Assertion.checkNotNull(storeManager);
		//-----
		this.dataStoreName = dataStoreName;
		this.securityManager = securityManager;
		this.storeManager = storeManager;
	}

	//===========================================================================
	//==================Gestion du rendu et des interactions=====================
	//===========================================================================

	/** {@inheritDoc} */
	@Override
	public String put(final Serializable data) {
		Assertion.checkNotNull(data, "Data is mandatory");
		//-----
		final String objectUUID = UUID.randomUUID().toString();
		final String tokenKey = makeTokenKey(objectUUID);
		storeManager.getKVStore().put(dataStoreName, tokenKey, data);
		return objectUUID; //We only return the object part.
	}

	/** {@inheritDoc} */
	@Override
	public Option<Serializable> get(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//-----
		final String tokenKey = makeTokenKey(objectUUID);
		return storeManager.getKVStore().find(dataStoreName, tokenKey, Serializable.class);
	}

	/** {@inheritDoc} */
	@Override
	public Option<Serializable> getAndRemove(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//-----
		final String tokenKey = makeTokenKey(objectUUID);
		final Option<Serializable> result = storeManager.getKVStore().find(dataStoreName, tokenKey, Serializable.class);
		if (result.isDefined()) {
			storeManager.getKVStore().remove(dataStoreName, tokenKey);
		}
		return result;
	}

	private String makeTokenKey(final String objectUUID) {
		final Option<UserSession> userSessionOption = securityManager.getCurrentUserSession();
		Assertion.checkArgument(userSessionOption.isDefined(), "UserSession is mandatory for security token");
		//-----
		return new StringBuilder(36 + 1 + 36)
				.append(getUserTokenPart()).append(":").append(objectUUID)
				.toString();
	}

	private String getUserTokenPart() {
		final Option<UserSession> userSessionOption = securityManager.getCurrentUserSession();
		Assertion.checkArgument(userSessionOption.isDefined(), "UserSession is mandatory for security token");
		//-----
		return userSessionOption.get().getSessionUUID().toString();
	}

}
