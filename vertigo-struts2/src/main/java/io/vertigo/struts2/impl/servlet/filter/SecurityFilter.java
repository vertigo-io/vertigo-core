package io.vertigo.struts2.impl.servlet.filter;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Option;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.persona.security.UserSession;
import io.vertigo.struts2.exception.SessionException;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtre de gestion des sessions utilisateurs bindées sur HTTP.
 * 
 * @author npiedeloup
 */
public final class SecurityFilter extends AbstractFilter {

	/**
	 * Nom de l'objet KUserSession dans la session J2EE
	 */
	private static final String USER_SESSION = "kasper.controller.Session";

	private static final String NO_AUTHENTIFICATION_PATTERN_PARAM_NAME = "url-no-authentification";

	/**
	 * Le gestionnaire de sécurité
	 */
	private KSecurityManager securityManager;

	private Option<Pattern> noAuthentificationPattern;

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		securityManager = Home.getComponentSpace().resolve(KSecurityManager.class);
		noAuthentificationPattern = parsePattern(getFilterConfig().getInitParameter(NO_AUTHENTIFICATION_PATTERN_PARAM_NAME));
	}

	/** {@inheritDoc} */
	@Override
	public void doMyFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		doSecurityFilter(!isUrlMatch(req, noAuthentificationPattern), req, res, chain);
	}

	private void doSecurityFilter(final boolean needsAuthentification, final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		final boolean hasSession = httpRequest.getSession(false) != null;

		// On récupère la session de l'utilisateur
		final UserSession user = obtainUserSession(httpRequest);

		try {
			// on place la session en ThreadLocal
			securityManager.startCurrentUserSession(user);

			// 1. Persistance de UserSession dans la session HTTP.
			bindUser(httpRequest, user);

			// 2. Vérification que l'utilisateur est authentifié si l'adresse demandée l'exige
			if (needsAuthentification && !user.isAuthenticated()) {
				/*
				 * Lance des exceptions - si la session a expiré - ou si aucune session utilisateur n'existe.
				 */
				httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
				//il ne faut pas continuer
				if (!hasSession) {
					//Par défaut on considère que la session a expirer
					throw new ServletException(new SessionException("Session expir�e"));
				}
			} else {
				// ---------------------------------------------------------------------
				chain.doFilter(request, response);
			}
		} finally {
			// On retire le user du ThreadLocal (il est déjà en session)
			securityManager.stopCurrentUserSession();
		}
	}

	// ==========================================================================
	// =================GESTION DE LA SESSION UTILISATEUR========================
	// ==========================================================================
	/**
	 * Lie l'utilisateur à la session en cours.
	 * 
	 * @param request Request
	 * @param user User
	 */
	private static void bindUser(final HttpServletRequest request, final UserSession user) {
		final HttpSession session = request.getSession(true);
		final Object o = session.getAttribute(USER_SESSION);
		if (o == null || !o.equals(user)) {
			session.setAttribute(USER_SESSION, user);
		}
	}

	/**
	 * Retourne la session utilisateur.
	 * 
	 * @return Session utilisateur
	 * @param request HTTPRequest
	 */
	private UserSession obtainUserSession(final HttpServletRequest request) {
		final HttpSession session = request.getSession(false);
		UserSession user = getUserSession(session);
		// Si la session user n'est pas créée on la crée
		if (user == null) {
			user = securityManager.createUserSession();
			if (session != null) {
				session.setAttribute(USER_SESSION, user);
			}
		}
		return user;
	}

	/**
	 * Récupération de l'utilisateur lié à la session.
	 * 
	 * @param session HttpSession
	 * @return UserSession Utilisateur bindé sur la session (peut être null)
	 */
	private static UserSession getUserSession(final HttpSession session) {
		return session == null ? null : (UserSession) session.getAttribute(USER_SESSION);
	}
}
