package io.vertigo.persona.impl.security;

import io.vertigo.core.config.Features;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;

public final class PersonaFeatures extends Features {

	public PersonaFeatures() {
		super("persona");
	}

	@Override
	protected void setUp() {
		//		getModuleConfigBuilder()
		//				.addComponent(VSecurityManager.class, VSecurityManagerImpl.class);
	}

	public PersonaFeatures withUserSession(final Class<? extends UserSession> userSessionClass) {
		getModuleConfigBuilder()
				.beginComponent(VSecurityManager.class, VSecurityManagerImpl.class)
				.addParam("userSessionClassName", userSessionClass.getName());
		return this;
	}
}
