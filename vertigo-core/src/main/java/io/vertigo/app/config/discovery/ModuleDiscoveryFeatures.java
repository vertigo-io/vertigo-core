package io.vertigo.app.config.discovery;

import io.vertigo.app.config.Features;
import io.vertigo.lang.Component;

/**
 * An abstract Feature with no configuration for discovering and registering components in a package tree.
 * Usage :
 *  - Extends this class
 *  - Provide a module name
 *  - Provide the package prefix to scan for components
 *  - Register this feature in your app's configuration (XML or Java)
 * @author mlaroche
 *
 */
public abstract class ModuleDiscoveryFeatures extends Features {

	protected ModuleDiscoveryFeatures(final String name) {
		super(name);
	}

	protected abstract String getPackageRoot();

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		//DAO + PAO + Services + WebServices
		ComponentDiscovery.registerComponents(Component.class, getPackageRoot(), getModuleConfigBuilder());
	}

}
