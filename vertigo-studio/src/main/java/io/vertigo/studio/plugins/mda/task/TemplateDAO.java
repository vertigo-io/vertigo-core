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
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.plugins.mda.FileConfig;

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
	private final Collection<TemplateTaskDefinition> taskDefinitions = new ArrayList<>();

	private final SearchIndexDefinition indexDefinition;
	private final Collection<TemplateFacetedQueryDefinition> facetedQueryDefinitions = new ArrayList<>();
	private final boolean hasOptions;

	/**
	 * Constructeur.
	 *
	 * @param dtDefinition DtDefinition de l'objet à générer
	 */
	TemplateDAO(final FileConfig taskConfiguration, final DtDefinition dtDefinition, final Collection<TaskDefinition> taskDefinitionCollection) {
		Assertion.checkNotNull(taskConfiguration);
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(taskDefinitionCollection);
		final String definitionPackageName = dtDefinition.getPackageName();
		final String packageNamePrefix = taskConfiguration.getProjectPackageName() + ".domain";
		Assertion.checkArgument(definitionPackageName.contains(packageNamePrefix), "Le nom du package {0}, doit commencer par le prefix normalise: {1}", definitionPackageName, packageNamePrefix);
		//-----
		this.dtDefinition = dtDefinition;
		//On construit le nom du package à partir du package de la DT dans le quel on supprime le début.
		packageName = taskConfiguration.getProjectPackageName() + ".dao" + definitionPackageName.substring(packageNamePrefix.length());

		boolean hasOption = false;
		for (final TaskDefinition taskDefinition : taskDefinitionCollection) {
			final TemplateTaskDefinition templateTaskDefinition = new TemplateTaskDefinition(taskDefinition);
			taskDefinitions.add(templateTaskDefinition);
			hasOption = hasOption || templateTaskDefinition.hasOptions();
		}
		hasOptions = hasOption;
		//TODO : find better than one dependency per behavior
		if (Home.getDefinitionSpace().getAllTypes().contains(SearchIndexDefinition.class)) {
			SearchIndexDefinition currentIndexDefinition = null;
			for (final SearchIndexDefinition tmpIndexDefinition : Home.getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
				if (tmpIndexDefinition.getKeyConceptDtDefinition().equals(dtDefinition)) {
					currentIndexDefinition = tmpIndexDefinition;
				}
			}
			indexDefinition = currentIndexDefinition;
			if (indexDefinition != null) {
				for (final FacetedQueryDefinition facetedQueryDefinition : Home.getDefinitionSpace().getAll(FacetedQueryDefinition.class)) {
					if (facetedQueryDefinition.getKeyConceptDtDefinition().equals(dtDefinition)) {
						final TemplateFacetedQueryDefinition templateFacetedQueryDefinition = new TemplateFacetedQueryDefinition(facetedQueryDefinition);
						facetedQueryDefinitions.add(templateFacetedQueryDefinition);
					}
				}
			}
		} else {
			indexDefinition = null;
		}
	}

	/**
	 * @return Simple Nom (i.e. sans le package) de la classe d'implémentation du DtObject
	 */
	public String getClassSimpleName() {
		return dtDefinition.getClassSimpleName() + "DAO";
	}

	/**
	 * @return Si l'entité est un keyConcept
	 */
	public boolean isKeyConcept() {
		return dtDefinition.getStereotype() == DtStereotype.KeyConcept;
	}

	/**
	 * @return Si l'entité possède le "behavior" Search
	 */
	public boolean hasSearchBehavior() {
		return indexDefinition != null;
	}

	/**
	 * @return Liste des facetedQueryDefinition
	 */
	public Collection<TemplateFacetedQueryDefinition> getFacetedQueryDefinitions() {
		return facetedQueryDefinitions;
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
	 * @return Nom simple de la classe du Dt
	 */
	public String getDtClassSimpleName() {
		return dtDefinition.getClassSimpleName();
	}

	/**
	 * @return Nom simple de la classe du Dt d'index
	 */
	public String getIndexDtClassSimpleName() {
		return indexDefinition.getIndexDtDefinition().getClassSimpleName();
	}

	/**
	 * @return Nom de la classe du Dt d'index
	 */
	public String getIndexDtClassCanonicalName() {
		return indexDefinition.getIndexDtDefinition().getClassCanonicalName();
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
	 * @return Si ce dao utilise au moins une option : vertigo.core.lang.Option
	 */
	public boolean isOptions() {
		return hasOptions;
	}

}
