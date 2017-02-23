package io.vertigo.dynamo.plugins.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.config.DefinitionResourceConfig;
import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.core.definition.dsl.dynamic.DslDefinition;
import io.vertigo.core.definition.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.core.definition.dsl.dynamic.DynamicRegistry;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.plugins.environment.loaders.Loader;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiLoader;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoader;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoader;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMLoader;
import io.vertigo.dynamo.plugins.environment.registries.DynamoDynamicRegistry;
import io.vertigo.lang.Assertion;

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
public class DynamoDefinitionProvider implements DefinitionProvider {

	private final Map<String, Loader> loadersByType = new HashMap<>();
	private final List<DefinitionResourceConfig> definitionResourceConfigs = new ArrayList<>();

	/**
	 * Constructeur injectable.
	 * @param resourceManager the component for finding resources
	 * @param encoding the encoding to use for reading ksp files
	 */
	@Inject
	public DynamoDefinitionProvider(final ResourceManager resourceManager, @Named("encoding") final Optional<String> encoding) {
		loadersByType.put("kpr", new KprLoader(resourceManager, encoding));
		loadersByType.put("oom", new OOMLoader(resourceManager));
		loadersByType.put("xmi", new EAXmiLoader(resourceManager));
		loadersByType.put("classes", new AnnotationLoader());

	}

	@Override
	public void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		Assertion.checkNotNull(definitionResourceConfig);
		//
		definitionResourceConfigs.add(definitionResourceConfig);
	}

	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return parse(definitionSpace);
	}

	/**
	 * @param definitionResourceConfigs List of resources (must be in a type managed by this loader)
	 */
	private List<DefinitionSupplier> parse(final DefinitionSpace definitionSpace) {

		//Création du repositoy des instances le la grammaire (=> model)
		final DynamicRegistry dynamoDynamicRegistry = new DynamoDynamicRegistry();
		final DslDefinitionRepository dslDefinitionRepository = new DslDefinitionRepository(dynamoDynamicRegistry);

		//--Enregistrement des types primitifs
		for (final DslDefinition dslDefinition : dynamoDynamicRegistry.getGrammar().getRootDefinitions()) {
			dslDefinitionRepository.addDefinition(dslDefinition);
		}
		for (final DefinitionResourceConfig definitionResourceConfig : definitionResourceConfigs) {
			final Loader loaderPlugin = loadersByType.get(definitionResourceConfig.getType());
			Assertion.checkNotNull(loaderPlugin, "This resource {0} can not be parse by these loaders : {1}", definitionResourceConfig, loadersByType.keySet());
			loaderPlugin.load(definitionResourceConfig.getPath(), dslDefinitionRepository);
		}

		return dslDefinitionRepository.solve(definitionSpace);
	}

}
