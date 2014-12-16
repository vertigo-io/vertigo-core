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
import io.vertigo.lang.Option;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateDtDefinition;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateMethodSql;
import io.vertigo.util.MapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Generate crebas.sql
 *
 * @author pchretien
 */
public final class SqlGeneratorPlugin extends AbstractGeneratorPlugin<DomainConfiguration> {
	private final boolean generateDrop;
	private final String baseCible;
	private final Option<String> tableSpaceData;
	private final Option<String> tableSpaceIndex;

	/**
	 * Constructeur.
	 *
	 * @param generateDrop Si on génère les Drop table dans le fichier SQL
	 * @param baseCible Type de base de données ciblé.
	 * @param tableSpaceData Nom du tableSpace des données
	 * @param tableSpaceIndex Nom du tableSpace des indexes
	 */
	@Inject
	public SqlGeneratorPlugin(
			@Named("generateDrop") final boolean generateDrop,
			@Named("baseCible") final String baseCible,
			@Named("tableSpaceData") final Option<String> tableSpaceData,
			@Named("tableSpaceIndex") final Option<String> tableSpaceIndex) {
		// ---------------------------------------------------------------------
		this.generateDrop = generateDrop;
		this.baseCible = baseCible;
		this.tableSpaceData = tableSpaceData;
		this.tableSpaceIndex = tableSpaceIndex;
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
		generateSql(domainConfiguration, resultBuilder);
	}

	private void generateSql(final DomainConfiguration domainConfiguration, final ResultBuilder resultBuilder) {
		final List<TemplateDtDefinition> list = new ArrayList<>(DomainUtil.getDtDefinitions().size());
		for (final DtDefinition dtDefinition : DomainUtil.sortAbsoluteDefinitionCollection(DomainUtil.getDtDefinitions())) {
			final TemplateDtDefinition templateDef = new TemplateDtDefinition(dtDefinition);
			list.add(templateDef);
		}
		final MapBuilder<String, Object> mapRootBuilder = new MapBuilder<String, Object>()
				.put("sql", new TemplateMethodSql())
				.put("dtDefinitions", list)
				.put("associations", DomainUtil.getAssociations())
				.put("drop", generateDrop)
				// Ne sert actuellement à rien, le sql généré étant le même. Prévu pour le futur
				.put("basecible", baseCible)
				// Oracle limite le nom des entités (index) à 30 charactères. Il faut alors tronquer les noms composés.
				.put("truncateNames", baseCible == "Oracle");

		mapRootBuilder.put("tableSpaceData", tableSpaceData.getOrElse(null));
		mapRootBuilder.put("tableSpaceIndex", tableSpaceIndex.getOrElse(null));
		final Map<String, Object> mapRoot = mapRootBuilder.build();

		createFileGenerator(domainConfiguration, mapRoot, "crebas", "sqlgen", ".sql", "templates/sql.ftl")
				.generateFile(resultBuilder);
	}

}
