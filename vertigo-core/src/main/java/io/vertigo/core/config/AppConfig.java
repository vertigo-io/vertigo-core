package io.vertigo.core.config;

import io.vertigo.lang.Assertion;

/*
 * @author pchretien
 */
public final class AppConfig {
	private final ComponentSpaceConfig componentSpaceConfig;
	private final DefinitionSpaceConfig definitionSpaceConfig;

	AppConfig(final ComponentSpaceConfig componentSpaceConfig, final DefinitionSpaceConfig definitionSpaceConfig) {
		Assertion.checkNotNull(componentSpaceConfig);
		Assertion.checkNotNull(definitionSpaceConfig);
		//---------------------------------------------------------------------
		this.componentSpaceConfig = componentSpaceConfig;
		this.definitionSpaceConfig = definitionSpaceConfig;
	}

	public DefinitionSpaceConfig getDefinitionSpaceConfig() {
		return definitionSpaceConfig;
	}

	public ComponentSpaceConfig getComponentSpaceConfig() {
		return componentSpaceConfig;
	}
}
