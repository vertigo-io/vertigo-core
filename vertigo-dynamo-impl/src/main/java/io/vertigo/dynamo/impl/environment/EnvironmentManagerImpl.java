package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.environment.EnvironmentManager;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Activeable;

import java.util.List;

import javax.inject.Inject;

/**
 * Manager de chargement de l'environnement. Ce manager ce paramètre par l'ajout
 * de plugins implémentant DynamicHandler. Chaque plugins permet d'enrichir la
 * grammaire et de transposer les DynamicDefinition lues dans les NameSpaces des
 * Managers idoines.
 * 
 * @author pchretien, npiedeloup
 */
public final class EnvironmentManagerImpl implements EnvironmentManager, Activeable {
	@Inject
	private List<LoaderPlugin> loaderPlugins;
	@Inject
	private List<DynamicRegistryPlugin> dynamicRegistryPlugins;

	/** {@inheritDoc} */
	public void start() {
		Home.getResourceSpace().addLoader(new Environment(dynamicRegistryPlugins, loaderPlugins));
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

}
