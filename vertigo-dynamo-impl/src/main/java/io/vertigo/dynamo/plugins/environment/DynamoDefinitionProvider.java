package io.vertigo.dynamo.plugins.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.app.config.DefinitionResourceConfig;
import io.vertigo.app.config.DefinitionSupplier;
import io.vertigo.core.definition.dsl.dynamic.DslDefinition;
import io.vertigo.core.definition.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.core.definition.dsl.dynamic.DynamicRegistry;
import io.vertigo.core.definition.loader.LoaderPlugin;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.EAXmiLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.OOMLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.DynamoDynamicRegistry;
import io.vertigo.lang.Assertion;

public class DynamoDefinitionProvider implements DefinitionProvider {

	private final Map<String, LoaderPlugin> loadersByType = new HashMap<>();
	private final List<DefinitionResourceConfig> definitionResourceConfigs = new ArrayList<>();

	@Inject
	public DynamoDefinitionProvider(final ResourceManager resourceManager) {
		loadersByType.put("kpr", new KprLoaderPlugin(resourceManager, Optional.empty())); // attention il y a un paramètre
		loadersByType.put("oom", new OOMLoaderPlugin(resourceManager));
		loadersByType.put("xmi", new EAXmiLoaderPlugin(resourceManager));
		loadersByType.put("classes", new AnnotationLoaderPlugin());

	}

	@Override
	public void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		Assertion.checkNotNull(definitionResourceConfig);
		//
		definitionResourceConfigs.add(definitionResourceConfig);
	}

	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return parse(definitionSpace, definitionResourceConfigs);
	}

	/**
	 * @param definitionResourceConfigs List of resources (must be in a type managed by this loader)
	 */
	private List<DefinitionSupplier> parse(final DefinitionSpace definitionSpace, final List<DefinitionResourceConfig> definitionResourceConfigs) {

		//Création du repositoy des instances le la grammaire (=> model)
		final DynamicRegistry dynamoDynamicRegistry = new DynamoDynamicRegistry();
		final DslDefinitionRepository dslDefinitionRepository = new DslDefinitionRepository(dynamoDynamicRegistry);

		//--Enregistrement des types primitifs
		for (final DslDefinition dslDefinition : dynamoDynamicRegistry.getGrammar().getRootDefinitions()) {
			dslDefinitionRepository.addDefinition(dslDefinition);
		}
		for (final DefinitionResourceConfig definitionResourceConfig : definitionResourceConfigs) {
			final LoaderPlugin loaderPlugin = loadersByType.get(definitionResourceConfig.getType());
			Assertion.checkNotNull(loaderPlugin, "This resource {0} can not be parse by these loaders : {1}", definitionResourceConfig, loadersByType.keySet());
			loaderPlugin.load(definitionResourceConfig.getPath(), dslDefinitionRepository);
		}

		return dslDefinitionRepository.solve(definitionSpace);
	}

}
