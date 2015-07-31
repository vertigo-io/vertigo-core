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
import io.vertigo.studio.plugins.mda.FileConfig;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateDtDefinition;
import io.vertigo.studio.plugins.mda.domain.templates.TemplateMethodSql;
import io.vertigo.util.MapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Generate crebas.sql
 *
 * @author pchretien
 */
public final class SqlGeneratorPlugin extends AbstractGeneratorPlugin {
	private final String targetSubDir;
	private final boolean generateDrop;
	private final String baseCible;
	private final Option<String> tableSpaceData;
	private final Option<String> tableSpaceIndex;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 * @param generateDrop Si on génère les Drop table dans le fichier SQL
	 * @param baseCible Type de base de données ciblé.
	 * @param tableSpaceData Nom du tableSpace des données
	 * @param tableSpaceIndex Nom du tableSpace des indexes
	 */
	@Inject
	public SqlGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir,
			@Named("generateDrop") final boolean generateDrop,
			@Named("baseCible") final String baseCible,
			@Named("tableSpaceData") final Option<String> tableSpaceData,
			@Named("tableSpaceIndex") final Option<String> tableSpaceIndex) {
		//-----
		this.targetSubDir = targetSubDir;
		this.generateDrop = generateDrop;
		this.baseCible = baseCible;
		this.tableSpaceData = tableSpaceData;
		this.tableSpaceIndex = tableSpaceIndex;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileConfig domainConfig, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(domainConfig);
		Assertion.checkNotNull(resultBuilder);
		//-----
		generateSql(domainConfig, resultBuilder);
	}

	private void generateSql(final FileConfig domainConfiguration, final ResultBuilder resultBuilder) {
		final List<TemplateDtDefinition> list = new ArrayList<>(DomainUtil.getDtDefinitions().size());
		for (final DtDefinition dtDefinition : DomainUtil.sortDefinitionCollection(DomainUtil.getDtDefinitions())) {
			final TemplateDtDefinition templateDef = new TemplateDtDefinition(dtDefinition);
			list.add(templateDef);
		}
		final MapBuilder<String, Object> mapRootBuilder = new MapBuilder<String, Object>()
				.put("sql", new TemplateMethodSql())
				.put("dtDefinitions", list)
				.put("simpleAssociations", DomainUtil.getSimpleAssociations())
				.put("nnAssociations", DomainUtil.getNNAssociations())
				.put("drop", generateDrop)
				// Ne sert actuellement à rien, le sql généré étant le même. Prévu pour le futur
				.put("basecible", baseCible)
				// Oracle limite le nom des entités (index) à 30 charactères. Il faut alors tronquer les noms composés.
				.put("truncateNames", "Oracle".equals(baseCible));
		if (tableSpaceData.isDefined()) {
			mapRootBuilder.put("tableSpaceData", tableSpaceData.get());
		}
		if (tableSpaceIndex.isDefined()) {
			mapRootBuilder.put("tableSpaceIndex", tableSpaceIndex.get());
		}
		final Map<String, Object> mapRoot = mapRootBuilder.build();

		createFileGenerator(domainConfiguration, mapRoot, "crebas", targetSubDir, "", ".sql", "domain/templates/sql.ftl")
				.generateFile(resultBuilder);
	}

}
