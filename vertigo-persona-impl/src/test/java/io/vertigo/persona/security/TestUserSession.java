package io.vertigo.persona.security;

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
	 * @return Liste des cles de securite et leur valeur.
	 */
	@Override
	public Map<String, String> getSecurityKeys() {
		return Collections.singletonMap("famId", "12");
	}
}
