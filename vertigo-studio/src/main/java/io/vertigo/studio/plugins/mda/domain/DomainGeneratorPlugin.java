package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
 * @version $Id: DomainGeneratorPlugin.java,v 1.7 2014/02/27 10:29:00 pchretien Exp $
 */
public final class DomainGeneratorPlugin extends AbstractGeneratorPlugin<DomainConfiguration> {
	private final boolean generateDtResources;
	private final boolean generateJpaAnnotations;
	private final boolean generateDtDefinitions;
	private final boolean generateDtObject;
	private final boolean generateSql;

	/**
	 * Constructeur.
	 *
	 * @param generateDtResources Si on génère les fichiers i18n pour MessageText des labels des champs 
	 * @param generateJpaAnnotations Si on ajoute les annotations JPA 
	 * @param generateDtDefinitions Si on génère le fichier fournissant la liste des classes de Dt
	 * @param generateDtObject Si on génère les classes des Dt
	 * @param generateSql Si on génére le crebase.sql
	 */
	@Inject
	public DomainGeneratorPlugin(@Named("generateDtResources") final boolean generateDtResources, @Named("generateJpaAnnotations") final boolean generateJpaAnnotations, @Named("generateDtDefinitions") final boolean generateDtDefinitions, @Named("generateDtObject") final boolean generateDtObject, @Named("generateSql") final boolean generateSql) {
		// ---------------------------------------------------------------------
		this.generateDtResources = generateDtResources;
		this.generateJpaAnnotations = generateJpaAnnotations;
		this.generateDtDefinitions = generateDtDefinitions;
		this.generateDtObject = generateDtObject;
		this.generateSql = generateSql;
	}

	/** {@inheritDoc} */
	public DomainConfiguration createConfiguration(final Properties properties) {
		return new DomainConfiguration(properties);
	}

	private Collection<DtDefinition> getDtDefinitions() {
		return sortDefinitionCollection(Home.getDefinitionSpace().getAll(DtDefinition.class));
	}

	private Map<String, Collection<DtDefinition>> getDtDefinitionCollectionMap() {
		return getDefinitionCollectionMap(getDtDefinitions());
	}

	/** {@inheritDoc} */
	public void generate(final DomainConfiguration domainConfiguration, final Result result) {
		Assertion.checkNotNull(domainConfiguration);
		Assertion.checkNotNull(result);
		// ---------------------------------------------------------------------
		/* Génération des ressources afférentes au DT. */
		if (generateDtResources) {
			generateDtResources(domainConfiguration, result);
		}
		/* Génération de la lgeneratee référençant toutes des définitions. */
		if (generateDtDefinitions) {
			generateDtDefinitions(domainConfiguration, result);
		}
		/* Générations des DTO. */
		if (generateDtObject) {
			generateDtObjects(domainConfiguration, result);
		}
		if (generateSql) {
			generateSql(domainConfiguration, result);
		}
	}

	private void generateDtDefinitions(final DomainConfiguration domainConfiguration, final Result result) {

		final Map<String, Object> mapRoot = new HashMap<>();
		mapRoot.put("packageName", domainConfiguration.getDomainPackage());
		mapRoot.put("classSimpleName", "DtDefinitions");
		mapRoot.put("dtDefinitions", getDtDefinitions());

		final FileGenerator super2java = getFileGenerator(domainConfiguration, mapRoot, "DtDefinitions", domainConfiguration.getDomainPackage(), ".java", "domain/dtdefinitions.ftl");
		super2java.generateFile(result, true);

	}

	private void generateDtObjects(final DomainConfiguration domainConfiguration, final Result result) {
		for (final DtDefinition dtDefinition : getDtDefinitions()) {
			generateDtObject(domainConfiguration, result, dtDefinition);
		}
	}

	private void generateDtObject(final DomainConfiguration domainConfiguration, final Result result, final DtDefinition dtDefinition) {
		final TemplateDtDefinition definition = new TemplateDtDefinition(dtDefinition);

		final Map<String, Object> mapRoot = new HashMap<>();
		mapRoot.put("dtDefinition", definition);
		mapRoot.put("annotations", new TemplateMethodAnnotations(generateJpaAnnotations));

		final FileGenerator super2java = getFileGenerator(domainConfiguration, mapRoot, definition.getClassSimpleName(), definition.getPackageName(), //
				".java", "domain/dto.ftl");
		super2java.generateFile(result, true);
	}

	private void generateDtResources(final DomainConfiguration domainConfiguration, final Result result) {
		final String simpleClassName = "DtResources";
		/**
		 * Génération des ressources afférentes au DT.
		 */
		for (final Entry<String, Collection<DtDefinition>> entry : getDtDefinitionCollectionMap().entrySet()) {
			final Collection<DtDefinition> dtDefinitionCollection = entry.getValue();
			Assertion.checkNotNull(dtDefinitionCollection);
			final String packageName = entry.getKey();

			if (domainConfiguration.isResourcesFileGenerated()) {

				final Map<String, Object> mapRoot = new HashMap<>();
				mapRoot.put("packageName", packageName);
				mapRoot.put("simpleClassName", simpleClassName);
				mapRoot.put("dtDefinitions", dtDefinitionCollection);

				final FileGenerator dtDefinitions2ResourceEnum = getFileGenerator(domainConfiguration, mapRoot, simpleClassName, packageName, ".java", "domain/resources.ftl");
				dtDefinitions2ResourceEnum.generateFile(result, true);

				final FileGenerator dtDefinitions2ResourceProperties = getFileGenerator(domainConfiguration, mapRoot, simpleClassName, packageName, ".properties", "domain/properties.ftl");
				dtDefinitions2ResourceProperties.generateFile(result, true);
			}
		}
	}

	private void generateSql(final DomainConfiguration domainConfiguration, final Result result) {
		final Map<String, Object> mapRoot = new HashMap<>();
		mapRoot.put("sql", new TemplateMethodSqlOracle());
		mapRoot.put("dtDefinitions", getDtDefinitions());
		mapRoot.put("oracle", true);
		final FileGenerator super2java = getFileGenerator(domainConfiguration, mapRoot, "crebas", "sqlgen", ".sql", "domain/sql.ftl");
		super2java.generateFile(result, true);
	}

	/**
	 * trie de la collection.
	 * @param definitionCollection collection à trier
	 * @return collection triée
	 */
	private static Collection<DtDefinition> sortDefinitionCollection(final Collection<DtDefinition> definitionCollection) {
		final List<DtDefinition> list = new ArrayList<>(definitionCollection);
		java.util.Collections.sort(list, new Comparator<DtDefinition>() {
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
}
