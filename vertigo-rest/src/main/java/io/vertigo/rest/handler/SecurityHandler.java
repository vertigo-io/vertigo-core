package io.vertigo.rest.handler;

import io.vertigo.kernel.lang.Option;
import io.vertigo.rest.exception.SessionException;
import io.vertigo.rest.exception.VSecurityException;
import io.vertigo.security.KSecurityManager;
import io.vertigo.security.UserSession;

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
		} else {
			// ---------------------------------------------------------------------
			return chain.handle(request, response);
		}
	}
}
