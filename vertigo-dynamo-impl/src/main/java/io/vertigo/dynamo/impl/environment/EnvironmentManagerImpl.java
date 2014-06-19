package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.environment.EnvironmentManager;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Activeable;

import java.util.ArrayList;
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
		// Pour des raisons d'optimisation mémoire on ne garde pas la liste des
		// plugins dans le manager.
		// Car ceux ci ne sont utilisé qu'une seule fois.
		load();
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

	private void load() {
		// Création de l'environnement.
		final Environment environment = new Environment(dynamicRegistryPlugins,	loaderPlugins);
		// Chargement des données.
		environment.load();
	}
}
