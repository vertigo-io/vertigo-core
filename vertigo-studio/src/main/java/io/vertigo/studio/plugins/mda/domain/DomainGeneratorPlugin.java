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

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.util.MapBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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
	private final boolean generateDtResourcesJS;
	private final boolean generateJpaAnnotations;
	private final boolean generateDtDefinitions;
	private final boolean generateJsDtDefinitions;
	private final boolean generateDtObject;
	private final boolean generateSql;
	private final boolean generateDrop;
	private final String baseCible;

	/**
	 * Constructeur.
	 *
	 * @param generateDtResources Si on génère les fichiers i18n pour MessageText des labels des champs
	 * @param generateDtResourcesJS Si on génère les fichiers i18n pour les labels des champs en JS
	 * @param generateJpaAnnotations Si on ajoute les annotations JPA
	 * @param generateDtDefinitions Si on génère le fichier fournissant la liste des classes de Dt
	 * @param generateDtObject Si on génère les classes des Dt
	 * @param generateJsDtDefinitions Si on génère les classes JS.
	 * @param generateSql Si on génére le crebase.sql
	 * @param generateDrop Si on génère les Drop table dans le fichier SQL
	 * @param baseCible Type de base de données ciblé.
	 */
	@Inject
	public DomainGeneratorPlugin(
			@Named("generateDtResources") final boolean generateDtResources,
			@Named("generateDtResourcesJS") final boolean generateDtResourcesJS,
			@Named("generateJpaAnnotations") final boolean generateJpaAnnotations,
			@Named("generateDtDefinitions") final boolean generateDtDefinitions,
			@Named("generateJsDtDefinitions") final boolean generateJsDtDefinitions,
			@Named("generateDtObject") final boolean generateDtObject,
			@Named("generateSql") final boolean generateSql,
			@Named("generateDrop") final boolean generateDrop,
			@Named("baseCible") final String baseCible) {
		// ---------------------------------------------------------------------
		this.generateDtResources = generateDtResources;
		this.generateDtResourcesJS = generateDtResourcesJS;
		this.generateJpaAnnotations = generateJpaAnnotations;
		this.generateDtDefinitions = generateDtDefinitions;
		this.generateJsDtDefinitions = generateJsDtDefinitions;
		this.generateDtObject = generateDtObject;
		this.generateSql = generateSql;
		this.generateDrop = generateDrop;
		this.baseCible = baseCible;
	}

	/** {@inheritDoc} */
	@Override
	public DomainConfiguration createConfiguration(final Properties properties) {
		return new DomainConfiguration(properties);
	}

	private static Collection<DtDefinition> getDtDefinitions() {
		return sortDefinitionCollection(Home.getDefinitionSpace().getAll(DtDefinition.class));
	}

	private static Map<String, Collection<DtDefinition>> getDtDefinitionCollectionMap() {
		return getDefinitionCollectionMap(getDtDefinitions());
	}

	private static Collection<AssociationDefinition> getAssociations() {
		return sortAssociationsCollection(Home.getDefinitionSpace().getAll(AssociationDefinition.class));
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final DomainConfiguration domainConfiguration, final Result result) {
		Assertion.checkNotNull(domainConfiguration);
		Assertion.checkNotNull(result);
		// ---------------------------------------------------------------------
		/* Génération des ressources afférentes au DT. */
		if (generateDtResources) {
			generateDtResources(domainConfiguration, result);
		}

		/* Génération des ressources afférentes au DT mais pour la partie JS.*/
		if (generateDtResourcesJS) {
			generateDtResourcesJS(domainConfiguration, result);
		}

		/* Génération de la lgeneratee référençant toutes des définitions. */
		if (generateDtDefinitions) {
			generateDtDefinitions(domainConfiguration, result);
		}

		/* Génération des fichiers javascripts référençant toutes les définitions. */
		if (generateJsDtDefinitions) {
			generateJsDtDefinitions(domainConfiguration, result);
		}

		/* Générations des DTO. */
		if (generateDtObject) {
			generateDtObjects(domainConfiguration, result);
		}
		if (generateSql) {
			generateSql(domainConfiguration, result);
		}
	}

	private static void generateDtDefinitions(final DomainConfiguration domainConfiguration, final Result result) {

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("packageName", domainConfiguration.getDomainPackage())
				.put("classSimpleName", domainConfiguration.getDomainDictionaryClassName())
				.put("dtDefinitions", getDtDefinitions())
				.build();

		createFileGenerator(domainConfiguration, mapRoot, domainConfiguration.getDomainDictionaryClassName(), domainConfiguration.getDomainPackage(), ".java", "dtdefinitions.ftl")
				.generateFile(result);

	}

	private static void generateJsDtDefinitions(final DomainConfiguration domainConfiguration, final Result result) {

		final List<TemplateDtDefinition> dtDefinitions = new ArrayList<>();
		for (final DtDefinition dtDefinition : getDtDefinitions()) {
			dtDefinitions.add(new TemplateDtDefinition(dtDefinition));
		}

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("packageName", domainConfiguration.getDomainPackage())
				.put("classSimpleName", domainConfiguration.getDomainDictionaryClassName())
				.put("dtDefinitions", dtDefinitions)
				.build();

		createFileGenerator(domainConfiguration, mapRoot, domainConfiguration.getDomainDictionaryClassName(), domainConfiguration.getDomainPackage(), ".js", "js.ftl")
				.generateFile(result);

	}

	private void generateDtObjects(final DomainConfiguration domainConfiguration, final Result result) {
		for (final DtDefinition dtDefinition : getDtDefinitions()) {
			generateDtObject(domainConfiguration, result, dtDefinition);
		}
	}

	private void generateDtObject(final DomainConfiguration domainConfiguration, final Result result, final DtDefinition dtDefinition) {
		final TemplateDtDefinition definition = new TemplateDtDefinition(dtDefinition);

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("dtDefinition", definition)
				.put("annotations", new TemplateMethodAnnotations(generateJpaAnnotations))
				.build();

		createFileGenerator(domainConfiguration, mapRoot, definition.getClassSimpleName(), definition.getPackageName(), ".java", "dto.ftl")
				.generateFile(result);
	}

	private static void generateDtResources(final DomainConfiguration domainConfiguration, final Result result) {
		final String simpleClassName = "DtResources";
		/**
		 * Génération des ressources afférentes au DT.
		 */
		for (final Entry<String, Collection<DtDefinition>> entry : getDtDefinitionCollectionMap().entrySet()) {
			final Collection<DtDefinition> dtDefinitionCollection = entry.getValue();
			Assertion.checkNotNull(dtDefinitionCollection);
			final String packageName = entry.getKey();

			final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
					.put("packageName", packageName)
					.put("simpleClassName", simpleClassName)
					.put("dtDefinitions", dtDefinitionCollection)
					.build();

			createFileGenerator(domainConfiguration, mapRoot, simpleClassName, packageName, ".java", "resources.ftl")
					.generateFile(result);

			createFileGenerator(domainConfiguration, mapRoot, simpleClassName, packageName, ".properties", "properties.ftl")
					.generateFile(result);
		}
	}

	/**
	 * Génère les ressources JS pour les traductions.
	 * @param domainConfiguration Configuration du domaine.
	 * @param result Fichier dans lequel est généré.
	 */
	private static void generateDtResourcesJS(final DomainConfiguration domainConfiguration, final Result result) {
		/**
		 * Génération des ressources afférentes au DT.
		 */
		for (final Entry<String, Collection<DtDefinition>> entry : getDtDefinitionCollectionMap().entrySet()) {
			String simpleClassName = entry.getClass().getName() + ".generated";

			final List<TemplateDtDefinition> dtDefinitions = new ArrayList<>();
			for (final DtDefinition dtDefinition : getDtDefinitions()) {
				dtDefinitions.add(new TemplateDtDefinition(dtDefinition));
				simpleClassName = (new TemplateDtDefinition(dtDefinition)).getClassSimpleNameCamelCase();
			}

			final Collection<DtDefinition> dtDefinitionCollection = entry.getValue();
			Assertion.checkNotNull(dtDefinitionCollection);
			final String packageName = entry.getKey();

			final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
					.put("packageName", packageName)
					.put("simpleClassName", simpleClassName)
					.put("dtDefinitions", dtDefinitions)
					.build();

			createFileGenerator(domainConfiguration, mapRoot, simpleClassName, packageName, ".js", "propertiesJS.ftl")
					.generateFile(result);
		}
	}

	private void generateSql(final DomainConfiguration domainConfiguration, final Result result) {
		final List<TemplateDtDefinition> list = new ArrayList<>(getDtDefinitions().size());
		for (final DtDefinition dtDefinition : sortAbsoluteDefinitionCollection(getDtDefinitions())) {
			final TemplateDtDefinition templateDef = new TemplateDtDefinition(dtDefinition);
			list.add(templateDef);
		}

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("sql", new TemplateMethodSql())
				.put("dtDefinitions", list)
				.put("associations", getAssociations())
				.put("drop", generateDrop)
				// Ne sert actuellement à rien, le sql généré étant le même. Prévu pour le futur
				.put("basecible", baseCible)
				// Oracle limite le nom des entités (index) à 30 charactères. Il faut alors tronquer les noms composés.
				.put("truncateNames", baseCible == "Oracle")
				.build();

		createFileGenerator(domainConfiguration, mapRoot, "crebas", "sqlgen", ".sql", "sql.ftl")
				.generateFile(result);
	}

	/**
	 * trie de la collection.
	 * @param definitionCollection collection à trier
	 * @return collection triée
	 */
	private static Collection<DtDefinition> sortDefinitionCollection(final Collection<DtDefinition> definitionCollection) {
		final List<DtDefinition> list = new ArrayList<>(definitionCollection);
		java.util.Collections.sort(list, new Comparator<DtDefinition>() {
			@Override
			public int compare(final DtDefinition definition1, final DtDefinition definition2) {
				return definition1.getClassCanonicalName().compareTo(definition2.getClassCanonicalName());
			}

			@Override
			public boolean equals(final Object obj) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int hashCode() {
				throw new UnsupportedOperationException();
			}
		});
		return list;
	}

	/**
	 * @param definitionCollection collection à traiter
	 * @return map ayant le package name en clef
	 */
	private static Map<String, Collection<DtDefinition>> getDefinitionCollectionMap(final Collection<DtDefinition> definitionCollection) {
		final Map<String, Collection<DtDefinition>> map = new LinkedHashMap<>();

		for (final DtDefinition definition : definitionCollection) {
			Collection<DtDefinition> dtDefinitions = map.get(definition.getPackageName());
			if (dtDefinitions == null) {
				dtDefinitions = new ArrayList<>();
				map.put(definition.getPackageName(), dtDefinitions);
			}
			dtDefinitions.add(definition);
		}
		return map;
	}

	private static Collection<DtDefinition> sortAbsoluteDefinitionCollection(final Collection<DtDefinition> definitionCollection) {
		final List<DtDefinition> list = new ArrayList<>(definitionCollection);
		java.util.Collections.sort(list, new Comparator<DtDefinition>() {
			@Override
			public int compare(final DtDefinition definition1, final DtDefinition definition2) {
				return definition1.getClassSimpleName().compareTo(definition2.getClassSimpleName());
			}

			@Override
			public boolean equals(final Object obj) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int hashCode() {
				throw new UnsupportedOperationException();
			}
		});
		return list;
	}

	private static Collection<AssociationDefinition> sortAssociationsCollection(final Collection<AssociationDefinition> associationCollection) {
		final List<AssociationDefinition> list = new ArrayList<>(associationCollection);
		java.util.Collections.sort(list, new Comparator<AssociationDefinition>() {
			@Override
			public int compare(final AssociationDefinition definition1, final AssociationDefinition definition2) {
				return definition1.getName().compareTo(definition2.getName());
			}

			@Override
			public boolean equals(final Object obj) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int hashCode() {
				throw new UnsupportedOperationException();
			}
		});
		return list;
	}

}
