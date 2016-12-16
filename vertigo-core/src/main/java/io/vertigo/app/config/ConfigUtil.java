package io.vertigo.app.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertigo.lang.Assertion;

final class ConfigUtil {
	private ConfigUtil() {
		//
	}

	static List<ComponentConfig> buildPluginConfigs(final List<PluginConfigBuilder> pluginConfigBuilders) {
		Assertion.checkNotNull(pluginConfigBuilders);
		//---
		final List<ComponentConfig> pluginConfigs = new ArrayList<>();
		final Set<String> pluginTypes = new HashSet<>();
		int index = 1;
		for (final PluginConfigBuilder pluginConfigBuilder : pluginConfigBuilders) {
			final boolean added = pluginTypes.add(pluginConfigBuilder.getPluginType());
			if (added) {
				//If added, its the first plugin to this type.
				pluginConfigBuilder.withIndex(0);
			} else {
				pluginConfigBuilder.withIndex(index);
				index++;
			}

			pluginConfigs.add(pluginConfigBuilder.build());
		}
		return pluginConfigs;
	}

}
