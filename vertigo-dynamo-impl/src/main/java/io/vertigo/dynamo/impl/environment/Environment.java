package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.kernel.di.configurator.ResourceConfig;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.resource.ResourceLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Environnement permettant de charger le Modèle.
 * Le Modèle peut être chargé de multiples façon :
 * - par lecture d'un fichier oom (poweramc),
 * - par lecture des annotations java présentes sur les beans,
 * - par lecture de fichiers ksp regoupés dans un projet kpr,
 * - ....
 *  Ces modes de chargement sont extensibles. 	
 *
 * @author pchretien
 */
final class Environment implements ResourceLoader {
	private final Map<String, Loader> loaders = new HashMap<>();
	private final List<DynamicRegistryPlugin> dynamicRegistryPlugins;

	/**
	 * Constructeur.
	 * @param dynamicModelRepository  DynamicModelRepository
	 */
	Environment(List<DynamicRegistryPlugin> dynamicRegistryPlugins, final List<LoaderPlugin> loaderPlugins) {
		Assertion.checkNotNull(dynamicRegistryPlugins);
		Assertion.checkNotNull(loaderPlugins);
		//---------------------------------------------------------------------
		this.dynamicRegistryPlugins = dynamicRegistryPlugins;
		//On enregistre les loaders 
		for (LoaderPlugin loaderPlugin : loaderPlugins) {
			loaders.put(loaderPlugin.getType(), loaderPlugin);
		}
	}

	public void parse(List<ResourceConfig> resourceConfigs) {
		final CompositeDynamicRegistry handler = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		final DynamicDefinitionRepository dynamicModelRepository = new DynamicDefinitionRepository(handler);

		//--Enregistrement des types primitifs
		final Entity dataTypeEntity = KernelGrammar.INSTANCE.getDataTypeEntity();
		for (final DataType type : DataType.values()) {
			final DynamicDefinition definition = dynamicModelRepository.createDynamicDefinitionBuilder(type.name(), dataTypeEntity, null).build();
			dynamicModelRepository.addDefinition(definition);
		}
		for (ResourceConfig resourceConfig : resourceConfigs) {
			Loader loader = loaders.get(resourceConfig.getType());
			Assertion.checkNotNull(loader, "This resource {0} can not be parse by this loader", resourceConfig);
			loader.load(resourceConfig.getPath(), dynamicModelRepository);
		}
		dynamicModelRepository.solve();
	}

	public Set<String> getTypes() {
		return Collections.unmodifiableSet(loaders.keySet());
	}
}
