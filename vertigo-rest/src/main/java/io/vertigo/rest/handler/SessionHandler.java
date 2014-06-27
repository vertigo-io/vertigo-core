package io.vertigo.rest.handler;

import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;
import io.vertigo.security.KSecurityManager;
import io.vertigo.security.UserSession;

import javax.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Session;

/**
 * Session handler.
 * Create and bind UserSession object with client.
 * @author npiedeloup
 */
public final class SessionHandler implements RouteHandler {
	/**
	 * UserSession attributeName in HttpSession.
	 */
	private static final String USER_SESSION = "vertigo.rest.Session";

	private final KSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param securityManager Security Manager
	 */
	@Inject
	public SessionHandler(final KSecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	/** {@inheritDoc} */
	public Object handle(final Request request, final Response response, final HandlerChain chain) throws SessionException, VSecurityException {
		final Session session = request.session(true); //obtain session (create if needed)
		final UserSession user = obtainUserSession(session);
		try {
			// Bind userSession to SecurityManager
			securityManager.startCurrentUserSession(user);

			return chain.handle(request, response);
		} catch (final VSecurityException e) {
			if (!session.isNew()) {
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
	 * @param request HTTPRequest
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
