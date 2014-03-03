package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.environment.EnvironmentManager;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Activeable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Manager de chargement de l'environnement.
 * Ce manager ce paramètre par l'ajout de plugins implémentant DynamicHandler.
 * Chaque plugins permet d'enrichir la grammaire et de transposer 
 * les DynamicDefinition lues dans les NameSpaces des Managers idoines.
 * @author pchretien, npiedeloup
 * @version $Id: EnvironmentManagerImpl.java,v 1.3 2013/10/22 12:30:39 pchretien Exp $
 */
public final class EnvironmentManagerImpl implements EnvironmentManager, Activeable {
	@Inject
	private List<LoaderPlugin> loaderPlugins;
	@Inject
	private List<DynamicRegistryPlugin> dynamicRegistryPlugins;

	/** {@inheritDoc} */
	public void start() {
		//Pour des raisons d'optimisation mémoire on ne garde pas la liste des plugins dans le manager.
		//Car ceux ci ne sont utilisé qu'une seule fois.
		load();
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

	private void load() {
		try {
			//Création de l'environnement.
			final Environment environment = createEnvironment();
			//Chargement des données.
			environment.load();
		} catch (final LoaderException e) {
			throw new VRuntimeException(e);
		}
	}

	/**
	 * Création d'un environnement
	 * @return Environnement
	 */
	private Environment createEnvironment() {
		final CompositeDynamicRegistry handler = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		final DynamicDefinitionRepository dynamicModelRepository = new DynamicDefinitionRepository(handler);

		//On enregistre les loaders 
		final List<LoaderPlugin> envLoaderPlugins = new ArrayList<>();
		//--Enregistrement des primitives du langage
		envLoaderPlugins.add(new KernelLoaderPlugin());
		envLoaderPlugins.addAll(loaderPlugins);

		//Création del'environnement
		return new Environment(dynamicModelRepository, envLoaderPlugins);
	}

}
