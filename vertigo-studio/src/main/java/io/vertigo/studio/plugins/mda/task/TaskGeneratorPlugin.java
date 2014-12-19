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
package io.vertigo.studio.plugins.mda.task;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.FileConfiguration;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.util.MapBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Génération des objets relatifs au module Task.
 *
 * @author pchretien
 */
public final class TaskGeneratorPlugin extends AbstractGeneratorPlugin {
	/** {@inheritDoc} */
	@Override
	public FileConfiguration createConfiguration(final Properties properties) {
		return new FileConfiguration(properties, "dao");
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileConfiguration taskConfiguration, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(taskConfiguration);
		Assertion.checkNotNull(resultBuilder);
		//---------------------------------------------------------------------
		generatePaos(taskConfiguration, resultBuilder);
		generateDaos(taskConfiguration, resultBuilder);
	}

	/**
	 * Génération de tous les PAOs.
	 */
	private static void generatePaos(final FileConfiguration taskConfiguration, final ResultBuilder resultBuilder) {
		//On liste des taches regroupées par Package.
		for (final Entry<String, List<TaskDefinition>> entry : buildPackageMap().entrySet()) {
			final Collection<TaskDefinition> taskDefinitionCollection = entry.getValue();
			if (!taskDefinitionCollection.isEmpty()) {
				final String packageName = entry.getKey();
				generatePao(taskConfiguration, resultBuilder, taskDefinitionCollection, packageName);
			}
		}
	}

	/**
	 * Génération de tous les DAOs.
	 */
	private static void generateDaos(final FileConfiguration taskConfiguration, final ResultBuilder resultBuilder) {
		for (final Entry<DtDefinition, List<TaskDefinition>> entry : builDtDefinitiondMap().entrySet()) {
			final DtDefinition dtDefinition = entry.getKey();
			if (dtDefinition.isPersistent()) {
				//Si DAO est persitant on génère son CRUD.
				generateDao(taskConfiguration, resultBuilder, dtDefinition, entry.getValue());
			}
		}
	}

	/**
	 * Génération d'un DAO c'est à dire des taches afférentes à un objet.
	 */
	private static void generateDao(final FileConfiguration taskConfiguration, final ResultBuilder resultBuilder, final DtDefinition dtDefinition, final Collection<TaskDefinition> taskDefinitionCollection) {
		final TemplateDAO dao = new TemplateDAO(taskConfiguration, dtDefinition, taskDefinitionCollection);

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("dao", dao)
				.build();

		createFileGenerator(taskConfiguration, mapRoot, dao.getClassSimpleName(), dao.getPackageName(), ".java", "dao.ftl")
				.generateFile(resultBuilder);
	}

	/**
	 *  Génération d'un PAO c'est à dire des taches afférentes à un package.
	 */
	private static void generatePao(final FileConfiguration taskConfiguration, final ResultBuilder resultBuilder, final Collection<TaskDefinition> taskDefinitionCollection, final String packageName) {
		final TemplatePAO pao = new TemplatePAO(taskConfiguration, taskDefinitionCollection, packageName);

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("pao", pao)
				.build();

		createFileGenerator(taskConfiguration, mapRoot, pao.getClassSimpleName(), pao.getPackageName(), ".java", "pao.ftl")
				.generateFile(resultBuilder);
	}

	/**
	 * Stratégie pour savoir si une tache est PAO ou DAO.
	 * Si la DT est non null DAO sinon PAO.
	 */
	private static DtDefinition getDtDefinition(final TemplateTaskDefinition templateTaskDefinition) {
		if (templateTaskDefinition.isOut()) {
			//si out on regarde si en sortie on a un DTO ou une DTC typé.
			final Domain outDomain = templateTaskDefinition.getOutAttribute().getDomain();
			if (outDomain.hasDtDefinition()) {
				return outDomain.getDtDefinition();
			}
		}
		//Si pad de donnée en sortie on considére PAO.
		return null;
	}

	private static Map<String, List<TaskDefinition>> buildPackageMap() {
		final Collection<TaskDefinition> allTaskDefinitions = Home.getDefinitionSpace().getAll(TaskDefinition.class);
		final Map<String, List<TaskDefinition>> taskDefinitionsMap = new LinkedHashMap<>();
		//---
		for (final TaskDefinition taskDefinition : allTaskDefinitions) {
			final TemplateTaskDefinition templateTaskDefinition = new TemplateTaskDefinition(taskDefinition);
			final DtDefinition dtDefinition = getDtDefinition(templateTaskDefinition);
			// Correction bug : task avec retour DtObject (non persistant) non générée
			//Les taches sont générées dans les pao
			// - si il n'esxiste pas de définition associées à la tache
			// - ou si la définition est considérée comme non persistante.
			final boolean pao = dtDefinition == null || !dtDefinition.isPersistent();
			if (pao) {
				//La tache est liée au package. (PAO)
				List<TaskDefinition> list = taskDefinitionsMap.get(taskDefinition.getPackageName());
				if (list == null) {
					list = new ArrayList<>();
					taskDefinitionsMap.put(taskDefinition.getPackageName(), list);
				}
				//on ajoute la tache aux taches du package.
				list.add(taskDefinition);
			}
		}
		return taskDefinitionsMap;

	}

	private static Map<DtDefinition, List<TaskDefinition>> builDtDefinitiondMap() {
		final Collection<TaskDefinition> allTaskDefinitions = Home.getDefinitionSpace().getAll(TaskDefinition.class);
		final Map<DtDefinition, List<TaskDefinition>> taskDefinitionsMap = new LinkedHashMap<>();

		//---
		//Par défaut, On crée pour chaque DT une liste vide des taches lui étant associées.
		final Collection<DtDefinition> dtDefinitions = Home.getDefinitionSpace().getAll(DtDefinition.class);
		for (final DtDefinition dtDefinition : dtDefinitions) {
			taskDefinitionsMap.put(dtDefinition, new ArrayList<TaskDefinition>());
		}
		//---
		for (final TaskDefinition taskDefinition : allTaskDefinitions) {
			final TemplateTaskDefinition templateTaskDefinition = new TemplateTaskDefinition(taskDefinition);

			final DtDefinition dtDefinition = getDtDefinition(templateTaskDefinition);
			final boolean dao = dtDefinition != null;
			if (dao) {
				//Dans le cas d'un DTO ou DTC en sortie on considère que la tache est liée au DAO.
				taskDefinitionsMap.get(dtDefinition).add(taskDefinition);
			}
		}
		return taskDefinitionsMap;

	}
}
