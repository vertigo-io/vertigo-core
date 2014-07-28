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
package io.vertigo.vega.impl.security;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.persona.security.UserSession;
import io.vertigo.vega.security.UiSecurityTokenManager;

import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Ui data security token.
 * Keep an security token by user and DtObject.
 *
 * @author npiedeloup
 */
public final class UiSecurityTokenManagerImpl implements UiSecurityTokenManager {

	private final KSecurityManager securityManager;
	//private final long timeoutSeconds;
	/** Object token, by */
	private final UiSecurityTokenCachePlugin uiSecurityTokenCachePlugin;

	//private final Map<String, Map<String, Object>> tokenMap = new HashMap<>();

	@Inject
	public UiSecurityTokenManagerImpl(final KSecurityManager securityManager, final UiSecurityTokenCachePlugin uiSecurityTokenCachePlugin) {
		Assertion.checkNotNull(securityManager);
		//Assertion.checkArgument(timeoutSeconds >= 1 && timeoutSeconds <= 172800, "Security token Timeout ({0}), should be set in seconds, positive and less than 2 days", timeoutSeconds);
		//---------------------------------------------------------------------
		this.securityManager = securityManager;
		this.uiSecurityTokenCachePlugin = uiSecurityTokenCachePlugin;
	}

	//---------------------------------------------------------------------------
	//------------------Gestion du rendu et des interactions---------------------
	//---------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public String put(final Serializable data) {
		Assertion.checkNotNull(data, "Data is mandatory");
		//---------------------------------------------------------------------
		final String objectUUID = UUID.randomUUID().toString();
		final String tokenKey = makeTokenKey(objectUUID);
		uiSecurityTokenCachePlugin.put(tokenKey, data);
		return objectUUID; //We only return the object part.
	}

	/** {@inheritDoc} */
	@Override
	public Serializable get(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//---------------------------------------------------------------------
		final String tokenKey = makeTokenKey(objectUUID);
		return uiSecurityTokenCachePlugin.get(tokenKey);
	}

	/** {@inheritDoc} */
	public Serializable getAndRemove(final String objectUUID) {
		Assertion.checkArgNotEmpty(objectUUID, "Security key is mandatory");
		//---------------------------------------------------------------------
		final String tokenKey = makeTokenKey(objectUUID);
		return uiSecurityTokenCachePlugin.getAndRemove(tokenKey);
	}

	private String makeTokenKey(final String objectUUID) {
		final Option<UserSession> userSessionOption = securityManager.getCurrentUserSession();
		Assertion.checkArgument(userSessionOption.isDefined(), "UserSession is mandatory for security token");
		//---------------------------------------------------------------------
		final StringBuilder sb = new StringBuilder(36 + 1 + 36);
		final String userUUID = userSessionOption.get().getSessionUUID().toString();
		sb.append(userUUID).append(":").append(objectUUID);
		final String tokenKey = sb.toString();
		return tokenKey;
	}
}
