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

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Objet utilisé par FreeMarker.
 * 
 * @author pchretien
 */
public final class TemplateDAO {
	private final DtDefinition dtDefinition;
	private final String packageName;
	private final Collection<TemplateTaskDefinition> taskDefinitions;
	private final boolean hasOptions;

	/**
	 * Constructeur.
	 * 
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	TemplateDAO(final TaskConfiguration taskConfiguration, final DtDefinition dtDefinition, final Collection<TaskDefinition> taskDefinitionCollection) {
		Assertion.checkNotNull(taskConfiguration);
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(taskDefinitionCollection);
		final String definitionPackageName = dtDefinition.getPackageName();
		final String packageNamePrefix = taskConfiguration.getProjectPackageName() + ".domain";
		Assertion.checkArgument(definitionPackageName.contains(packageNamePrefix), "Le nom du package {0}, doit commencer par le prefix normalise: {1}", definitionPackageName, packageNamePrefix);
		// -----------------------------------------------------------------
		this.dtDefinition = dtDefinition;
		//On construit le nom du package à partir du package de la DT dans le quel on supprime le début.
		packageName = taskConfiguration.getDaoPackage() + definitionPackageName.substring(packageNamePrefix.length());

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
		return dtDefinition.getClassSimpleName() + "DAO";
	}

	/**
	 * @return Type de la PK
	 */
	public String getPkFieldType() {
		return dtDefinition.getIdField().get().getDomain().getDataType().getJavaClass().getCanonicalName();
	}

	/**
	 * @return Nom de la classe du Dt
	 */
	public String getDtClassCanonicalName() {
		return dtDefinition.getClassCanonicalName();
	}

	/**
	 * @return Nom du package
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Liste des tasks
	 */
	public Collection<TemplateTaskDefinition> getTaskDefinitions() {
		return taskDefinitions;
	}

	/**
	 * @return Si ce dao utilise au moins une option : vertigo.kernel.lang.Option
	 */
	public boolean isOptions() {
		return hasOptions;
	}

}
