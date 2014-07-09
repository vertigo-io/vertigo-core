package io.vertigo.rest;

import io.vertigo.persona.security.UserSession;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public final class TestUserSession extends UserSession {
	private static final long serialVersionUID = 1L;

	@Override
	public Locale getLocale() {
		return Locale.FRANCE;
	}

	/**
	 * Gestion de la sécurité.
	 * @return Liste des clés de sécurité et leur valeur.
	 */
	@Override
	public Map<String, String> getSecurityKeys() {
		return Collections.singletonMap("famId", "12");
	}
}
