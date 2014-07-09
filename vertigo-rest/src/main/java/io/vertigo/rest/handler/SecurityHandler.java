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
package io.vertigo.rest.handler;

import io.vertigo.kernel.lang.Option;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.persona.security.UserSession;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;

import javax.inject.Inject;

import spark.Request;
import spark.Response;

/**
 * Security handler.
 * Ensure user is authenticated, throw VSecurityException if not.
 * @author npiedeloup
 */
public final class SecurityHandler implements RouteHandler {

	private final KSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param securityManager Security Manager
	 */
	@Inject
	public SecurityHandler(final KSecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	/** {@inheritDoc} */
	public Object handle(final Request request, final Response response, final HandlerChain chain) throws VSecurityException, SessionException {
		// 2. Check user is authentified
		final Option<UserSession> userSessionOption = securityManager.getCurrentUserSession();
		if (userSessionOption.isEmpty() || !userSessionOption.get().isAuthenticated()) {
			throw new VSecurityException("User unauthentified");
		}
		return chain.handle(request, response);
	}
}
