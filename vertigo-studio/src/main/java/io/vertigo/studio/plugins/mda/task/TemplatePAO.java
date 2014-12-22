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

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.FileConfiguration;
import io.vertigo.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author pchretien
 */
public final class TemplatePAO {
	private final String packageName;
	private final String className;
	private final Collection<TemplateTaskDefinition> taskDefinitions;
	private final boolean hasOptions;

	/**
	 * Constructeur.
	 */
	TemplatePAO(final FileConfiguration taskConfiguration, final Collection<TaskDefinition> taskDefinitionCollection, final String packageName) {
		Assertion.checkNotNull(taskConfiguration);
		Assertion.checkNotNull(taskDefinitionCollection);
		Assertion.checkArgument(!taskDefinitionCollection.isEmpty(), "Aucune tache dans le package {0}", packageName);
		Assertion.checkNotNull(packageName);
		//-----
		this.packageName = packageName;

		className = getLastPackagename(packageName) + "PAO";
		boolean hasOption = false;
		taskDefinitions = new ArrayList<>();
		for (final TaskDefinition taskDefinition : taskDefinitionCollection) {
			final TemplateTaskDefinition templateTaskDefinition = new TemplateTaskDefinition(taskDefinition);
			taskDefinitions.add(templateTaskDefinition);
			hasOption = hasOption || templateTaskDefinition.hasOptions();
		}
		hasOptions = hasOption;
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return className;
	}

	/**
	 * @return Liste des tasks
	 */
	public Collection<TemplateTaskDefinition> getTaskDefinitions() {
		return Collections.unmodifiableCollection(taskDefinitions);
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Retourne le nom du package feuille à partir d'un nom complet de package.
	 * exemple : org.company.sugar > sugar
	 * @param packageName Nom de package
	 * @return Nom du package feuille à partir d'un nom complet de package
	 */
	private static String getLastPackagename(final String packageName) {
		String lastPackageName = packageName;
		if (lastPackageName.indexOf('.') != -1) {
			lastPackageName = lastPackageName.substring(lastPackageName.lastIndexOf('.') + 1);
		}
		return StringUtil.first2UpperCase(lastPackageName);
	}

	/**
	 * @return Si ce pao utilise au moins une Option : vertigo.core.lang.Option
	 */
	public boolean isOptions() {
		return hasOptions;
	}
}
