package io.vertigo.app.config.discovery;

import io.vertigo.app.config.Features;
import io.vertigo.lang.Component;

public abstract class ModuleDiscoveryFeatures extends Features {

	protected ModuleDiscoveryFeatures(final String name) {
		super(name);
	}

	protected abstract String getPackageRoot();

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {

		//Services
		ComponentDiscovery.registerComponents(Component.class, getPackageRoot(), getModuleConfigBuilder());

	}

}
