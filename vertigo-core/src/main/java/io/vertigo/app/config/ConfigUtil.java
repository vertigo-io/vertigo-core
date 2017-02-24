package io.vertigo.app.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.vertigo.lang.Assertion;

final class ConfigUtil {
	private ConfigUtil() {
		//
	}

	static List<ComponentConfig> buildConfigs(final List<PluginConfig> pluginConfigs) {
		Assertion.checkNotNull(pluginConfigs);
		//---
		final List<ComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> pluginTypes = new HashSet<>();

		int index = 1;
		for (final PluginConfig pluginConfig : pluginConfigs) {
			final boolean added = pluginTypes.add(pluginConfig.getPluginType());
			final String id;
			if (added) {
				id = pluginConfig.getPluginType();
			} else {
				id = pluginConfig.getPluginType() + '#' + index;
				index++;
			}
			componentConfigs.add(
					new ComponentConfig(id,
							Optional.empty(),
							pluginConfig.getImplClass(),
							pluginConfig.getParams()));
		}
		return componentConfigs;
	}

}
