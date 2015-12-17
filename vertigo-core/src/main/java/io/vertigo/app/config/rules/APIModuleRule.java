package io.vertigo.app.config.rules;

import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.ModuleRule;
import io.vertigo.lang.VSystemException;

/**
 * Rule : all components of a module must have an API.
 *
 * @author pchretien
 */
public final class APIModuleRule implements ModuleRule {
	/** {@inheritDoc} */
	@Override
	public void check(final ModuleConfig moduleConfig) {
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			if (componentConfig.getApiClass().isEmpty()) {
				throw new VSystemException("api rule : all components of module '{0}' must have an api. Component '{1}' doesn't respect this rule.", moduleConfig, componentConfig);
			}
		}
	}
}
