package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

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
public final class Environment {
	//	/**
	//	 * Etat de l'environnement.
	//	 */
	//	enum Mode {
	//		/**
	//		 * Lors de la génération
	//		 */
	//		BUILD,
	//		/**
	//		 * En production.
	//		 */
	//		RUN, 
	//		/**
	//		 * En test.
	//		 */
	//		TEST 
	//	}

	private final List<Loader> loaders;
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
		this.loaders = new ArrayList<>();
		//--Enregistrement des primitives du langage
		this.loaders.add(new KernelLoader());
		this.loaders.addAll(loaderPlugins);
		
		final CompositeDynamicRegistry handler = new CompositeDynamicRegistry(dynamicRegistryPlugins);

		//Création du repositoy des instances le la grammaire (=> model)
		this.dynamicModelRepository = new DynamicDefinitionRepository(handler);
	}

	//		final NameSpace nameSpace = Home.getNameSpace();
	//		for (final Class<? extends Definition> definitionClass : nameSpace.getDefinitionClasses()) {
	//			System.out.println("######" + definitionClass.getSimpleName());
	//			for (final Definition definition : nameSpace.getDefinitions(definitionClass)) {
	//				System.out.println("     # " + definition.getName());
	//			}
	//		}

	void load()  {
		//On parcourt tous les loaders
		//On charge le Modèle dans le référentiel central
		for (final Loader loader : loaders) {
			loader.load(dynamicModelRepository);
		}

		//On résout les références
		dynamicModelRepository.solve();
	}
}
