package io.vertigo.core.config;

import io.vertigo.lang.Assertion;

import java.util.Map;

/*
 * @author pchretien
 */
public final class AppConfig {
	private final ComponentSpaceConfig componentSpaceConfig;
	private final DefinitionSpaceConfig definitionSpaceConfig;
	private final Map<String, String> params;

	AppConfig(final Map<String, String> params, final ComponentSpaceConfig componentSpaceConfig, final DefinitionSpaceConfig definitionSpaceConfig) {
		Assertion.checkNotNull(params);
		Assertion.checkNotNull(componentSpaceConfig);
		Assertion.checkNotNull(definitionSpaceConfig);
		//---------------------------------------------------------------------
		this.componentSpaceConfig = componentSpaceConfig;
		this.definitionSpaceConfig = definitionSpaceConfig;
		this.params = params;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public DefinitionSpaceConfig getDefinitionSpaceConfig() {
		return definitionSpaceConfig;
	}

	public ComponentSpaceConfig getComponentSpaceConfig() {
		return componentSpaceConfig;
	}
}
