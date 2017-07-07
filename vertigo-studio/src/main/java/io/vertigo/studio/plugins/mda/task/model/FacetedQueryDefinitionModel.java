/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.task.model;

import io.vertigo.core.definition.DefinitionUtil;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

/**
 * Génération des classes/méthodes des taches de type DAO.
 *
 * @author pchretien
 */
public final class FacetedQueryDefinitionModel {
	private final FacetedQueryDefinition facetedQueryDefinition;
	private final String simpleName;
	private final String criteriaClassCanonicalName;

	FacetedQueryDefinitionModel(final FacetedQueryDefinition facetedQueryDefinition) {
		Assertion.checkNotNull(facetedQueryDefinition);
		//-----
		this.facetedQueryDefinition = facetedQueryDefinition;
		simpleName = StringUtil.constToUpperCamelCase(DefinitionUtil.getLocalName(facetedQueryDefinition.getName(), FacetedQueryDefinition.class));
		criteriaClassCanonicalName = obtainCriteriaClassCanonicalName();
	}

	/**
	 * @return Nom local CamelCase de la facetedQueryDefinition
	 */
	public String getSimpleName() {
		return simpleName;
	}

	/**
	 * @return Urn de la facetedQueryDefinition
	 */
	public String getUrn() {
		return facetedQueryDefinition.getName();
	}

	/**
	 * @return Nom de la classe du criteria
	 */
	public String getCriteriaClassCanonicalName() {
		return criteriaClassCanonicalName;
	}

	private String obtainCriteriaClassCanonicalName() {
		final DataType domainDataType = facetedQueryDefinition.getCriteriaDomain().getDataType();
		final String domainClassName;
		switch (domainDataType) {
			case Boolean:
			case Double:
			case Integer:
			case Long:
			case String:
				domainClassName = domainDataType.name();
				break;
			case Date:
			case BigDecimal:
				domainClassName = domainDataType.getJavaClass().getCanonicalName();
				break;
			case DtObject:
				domainClassName = facetedQueryDefinition.getCriteriaDomain().getDtDefinition().getClassCanonicalName();
				break;
			case DtList:
			case DataStream:
			default:
				throw new IllegalArgumentException("Domain " + facetedQueryDefinition.getCriteriaDomain().getName() + " can't be use for a searchCriteria : use DtObject or primitive");
		}
		return domainClassName;
	}

}
