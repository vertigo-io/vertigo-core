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
 * @version $Id: Environment.java,v 1.3 2013/10/22 12:30:39 pchretien Exp $
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

	private final List<LoaderPlugin> loaderPlugins;
	private final DynamicDefinitionRepository dynamicModelRepository;

	/**
	 * Constructeur.
	 * @param dynamicModelRepository  DynamicModelRepository
	 */
	Environment(final DynamicDefinitionRepository dynamicModelRepository, final List<LoaderPlugin> loaderPlugins) {
		Assertion.checkNotNull(dynamicModelRepository);
		Assertion.checkNotNull(loaderPlugins);
		//---------------------------------------------------------------------
		this.dynamicModelRepository = dynamicModelRepository;
		this.loaderPlugins = new ArrayList<>(loaderPlugins);
	}

	//		final NameSpace nameSpace = Home.getNameSpace();
	//		for (final Class<? extends Definition> definitionClass : nameSpace.getDefinitionClasses()) {
	//			System.out.println("######" + definitionClass.getSimpleName());
	//			for (final Definition definition : nameSpace.getDefinitions(definitionClass)) {
	//				System.out.println("     # " + definition.getName());
	//			}
	//		}

	void load() throws LoaderException {
		//On parcourt tous les loaders
		//On charge le Modèle dans le référentiel central
		for (final LoaderPlugin loaderPlugin : loaderPlugins) {
			loaderPlugin.load(dynamicModelRepository);
		}

		//On résout les références
		dynamicModelRepository.solve();
	}
}
