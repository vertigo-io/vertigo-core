package io.vertigo.core.config;

import io.vertigo.lang.Assertion;

import java.util.Map;
import java.util.Properties;

/*
 * @author pchretien
 */
public final class AppConfig {
	private final ComponentSpaceConfig componentSpaceConfig;
	private final DefinitionSpaceConfig definitionSpaceConfig;
	private final Map<String, String> params;
	private final Properties envParams;

	AppConfig(final Properties envParams, final Map<String, String> params, final ComponentSpaceConfig componentSpaceConfig, final DefinitionSpaceConfig definitionSpaceConfig) {
		Assertion.checkNotNull(envParams);
		Assertion.checkNotNull(params);
		Assertion.checkNotNull(componentSpaceConfig);
		Assertion.checkNotNull(definitionSpaceConfig);
		//---------------------------------------------------------------------
		this.componentSpaceConfig = componentSpaceConfig;
		this.definitionSpaceConfig = definitionSpaceConfig;
		this.params = params;
		this.envParams = envParams;
	}

	public Properties getEnvParams() {
		return envParams;
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
