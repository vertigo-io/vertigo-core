/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateDtDefinition;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateMethodAnnotations;
import io.vertigo.util.MapBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Génération des objets relatifs au module Domain.
 *
 * @author pchretien
 */
public final class DomainGeneratorPlugin extends AbstractGeneratorPlugin<DomainConfiguration> {
	private final boolean generateDtResources;
	private final boolean generateJpaAnnotations;
	private final boolean generateDtDefinitions;
	private final boolean generateDtObject;

	/**
	 * Constructeur.
	 *
	 * @param generateDtResources Si on génère les fichiers i18n pour MessageText des labels des champs
	 * @param generateJpaAnnotations Si on ajoute les annotations JPA
	 * @param generateDtDefinitions Si on génère le fichier fournissant la liste des classes de Dt
	 * @param generateDtObject Si on génère les classes des Dt
	 */
	@Inject
	public DomainGeneratorPlugin(
			@Named("generateDtResources") final boolean generateDtResources,
			@Named("generateJpaAnnotations") final boolean generateJpaAnnotations,
			@Named("generateDtDefinitions") final boolean generateDtDefinitions,
			@Named("generateDtObject") final boolean generateDtObject) {
		// ---------------------------------------------------------------------
		this.generateDtResources = generateDtResources;
		this.generateJpaAnnotations = generateJpaAnnotations;
		this.generateDtDefinitions = generateDtDefinitions;
		this.generateDtObject = generateDtObject;
	}

	/** {@inheritDoc} */
	@Override
	public DomainConfiguration createConfiguration(final Properties properties) {
		return new DomainConfiguration(properties);
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final DomainConfiguration domainConfiguration, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(domainConfiguration);
		Assertion.checkNotNull(resultBuilder);
		// ---------------------------------------------------------------------
		/* Génération des ressources afférentes au DT. */
		if (generateDtResources) {
			generateDtResources(domainConfiguration, resultBuilder);
		}

		/* Génération de la lgeneratee référençant toutes des définitions. */
		if (generateDtDefinitions) {
			generateDtDefinitions(domainConfiguration, resultBuilder);
		}

		/* Générations des DTO. */
		if (generateDtObject) {
			generateDtObjects(domainConfiguration, resultBuilder);
		}

	}

	private static void generateDtDefinitions(final DomainConfiguration domainConfiguration, final ResultBuilder resultBuilder) {

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("packageName", domainConfiguration.getPackageName())
				.put("classSimpleName", domainConfiguration.getDomainDictionaryClassName())
				.put("dtDefinitions", DomainUtil.getDtDefinitions())
				.build();

		createFileGenerator(domainConfiguration, mapRoot, domainConfiguration.getDomainDictionaryClassName(), domainConfiguration.getPackageName(), ".java", "templates/dtdefinitions.ftl")
				.generateFile(resultBuilder);

	}

	private void generateDtObjects(final DomainConfiguration domainConfiguration, final ResultBuilder resultBuilder) {
		for (final DtDefinition dtDefinition : DomainUtil.getDtDefinitions()) {
			generateDtObject(domainConfiguration, resultBuilder, dtDefinition);
		}
	}

	private void generateDtObject(final DomainConfiguration domainConfiguration, final ResultBuilder resultBuilder, final DtDefinition dtDefinition) {
		final TemplateDtDefinition definition = new TemplateDtDefinition(dtDefinition);

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("dtDefinition", definition)
				.put("annotations", new TemplateMethodAnnotations(generateJpaAnnotations))
				.build();

		createFileGenerator(domainConfiguration, mapRoot, definition.getClassSimpleName(), definition.getPackageName(), ".java", "templates/dto.ftl")
				.generateFile(resultBuilder);
	}

	private static void generateDtResources(final DomainConfiguration domainConfiguration, final ResultBuilder resultBuilder) {
		final String simpleClassName = "DtResources";
		/**
		 * Génération des ressources afférentes au DT.
		 */
		for (final Entry<String, Collection<DtDefinition>> entry : DomainUtil.getDtDefinitionCollectionMap().entrySet()) {
			final Collection<DtDefinition> dtDefinitionCollection = entry.getValue();
			Assertion.checkNotNull(dtDefinitionCollection);
			final String packageName = entry.getKey();

			final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
					.put("packageName", packageName)
					.put("simpleClassName", simpleClassName)
					.put("dtDefinitions", dtDefinitionCollection)
					.build();

			createFileGenerator(domainConfiguration, mapRoot, simpleClassName, packageName, ".java", "templates/resources.ftl")
					.generateFile(resultBuilder);

			createFileGenerator(domainConfiguration, mapRoot, simpleClassName, packageName, ".properties", "templates/properties.ftl")
					.generateFile(resultBuilder);
		}
	}
}
