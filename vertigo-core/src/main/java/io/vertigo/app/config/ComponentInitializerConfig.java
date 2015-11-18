package io.vertigo.app.config;

import io.vertigo.core.spaces.component.ComponentInitializer;
import io.vertigo.lang.Assertion;

public final class ComponentInitializerConfig {
	private final Class<? extends ComponentInitializer> componentInitializerClass;

	/**
	 * @param componentInitializerClass Class de l'initialiseur du composant
	 */
	ComponentInitializerConfig(final Class<? extends ComponentInitializer> componentInitializerClass) {
		Assertion.checkNotNull(componentInitializerClass);
		//-----
		this.componentInitializerClass = componentInitializerClass;
	}

	/**
	 * @return Classe d'initialisation du composant. (Nullable)
	 */
	public Class<? extends ComponentInitializer> getInitializerClass() {
		return componentInitializerClass;
	}
}
