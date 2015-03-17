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
package io.vertigo.vega.plugins.rest.handler;

import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.persona.security.UserSession;
import io.vertigo.vega.impl.rest.RestHandlerPlugin;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;

import javax.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Session;

/**
 * Session handler.
 * Create and bind UserSession object with client.
 * @author npiedeloup
 */
public final class SessionRestHandlerPlugin implements RestHandlerPlugin {
	/**
	 * UserSession attributeName in HttpSession.
	 */
	private static final String USER_SESSION = "vertigo.rest.Session";

	private final VSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param securityManager Security Manager
	 */
	@Inject
	public SessionRestHandlerPlugin(final VSecurityManager securityManager) {
		Assertion.checkNotNull(securityManager);
		//-----
		this.securityManager = securityManager;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final EndPointDefinition endPointDefinition) {
		return endPointDefinition.isNeedSession();
	}

	/** {@inheritDoc} */
	@Override
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws SessionException, VSecurityException {
		final Session session = request.session(true); //obtain session (create if needed)
		final UserSession user = obtainUserSession(session);
		try {
			// Bind userSession to SecurityManager
			securityManager.startCurrentUserSession(user);

			return chain.handle(request, response, routeContext);
		} catch (final VSecurityException e) {
			if (session.isNew()) {
				//If session was just created, we translate securityException as a Session expiration.
				throw (SessionException) new SessionException("Session has expired").initCause(e);
			}
			throw e;
		} finally {
			// Unbind userSession to SecurityManager
			securityManager.stopCurrentUserSession();
		}
	}

	// ==========================================================================
	// =================GESTION DE LA SESSION UTILISATEUR========================
	// ==========================================================================

	/**
	 * Retourne la session utilisateur.
	 *
	 * @return Session utilisateur
	 */
	private UserSession obtainUserSession(final Session session) {
		UserSession user = (UserSession) session.attribute(USER_SESSION);
		// Si la session user n'est pas créée on la crée
		if (user == null) {
			user = securityManager.createUserSession();
			session.attribute(USER_SESSION, user);
		}
		return user;
	}
}
