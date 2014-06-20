package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.resource.ResourceLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private final Map<String, Loader> loaders;
	private final DynamicDefinitionRepository dynamicModelRepository;

	/**
	 * Constructeur.
	 * @param dynamicModelRepository  DynamicModelRepository
	 */
	Environment(List<DynamicRegistryPlugin> dynamicRegistryPlugins, final List<LoaderPlugin> loaderPlugins) {
		Assertion.checkNotNull(dynamicRegistryPlugins);
		Assertion.checkNotNull(loaderPlugins);
		//---------------------------------------------------------------------
		//On enregistre les loaders 
		this.loaders = new HashMap<>();
		for (LoaderPlugin loaderPlugin : loaderPlugins) {
			loaders.put(loaderPlugin.getType(), loaderPlugin);
		}

		final CompositeDynamicRegistry handler = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		this.dynamicModelRepository = new DynamicDefinitionRepository(handler);

		//--Enregistrement des types primitifs
		final Entity dataTypeEntity = KernelGrammar.INSTANCE.getDataTypeEntity();
		for (final DataType type : DataType.values()) {
			final DynamicDefinition definition = dynamicModelRepository.createDynamicDefinitionBuilder(type.name(), dataTypeEntity, null).build();
			dynamicModelRepository.addDefinition(definition);
		}
	}

	//		final NameSpace nameSpace = Home.getNameSpace();
	//		for (final Class<? extends Definition> definitionClass : nameSpace.getDefinitionClasses()) {
	//			System.out.println("######" + definitionClass.getSimpleName());
	//			for (final Definition definition : nameSpace.getDefinitions(definitionClass)) {
	//				System.out.println("     # " + definition.getName());
	//			}
	//		}

	public void add(Map<String, String> resources) { /*path, type*/
		for (Entry<String, String> entry : resources.entrySet()) {
			Loader loader = loaders.get(entry.getValue());
			if (loader != null) {
				loader.load(entry.getKey(), dynamicModelRepository);
			}
		}
	}

	public void solve() {
		//On résout les références
		dynamicModelRepository.solve();
	}

}
