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
package io.vertigo.dynamock.facet;

import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetDefinitionByRangeBuilder;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamox.search.DefaultListFilterBuilder;
import io.vertigo.lang.MessageText;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration des facettes de l'objet de test Car.
 * @author npiedeloup
 */
public final class CarFacetInitializer {
	public static final String FCT_DESCRIPTION_CAR = "FCT_DESCRIPTION_CAR";

	public static final String FCT_MAKE_CAR = "FCT_MAKE_CAR";

	public static final String FCT_YEAR_CAR = "FCT_YEAR_CAR";

	public static final String FCT_CAR_SUFFIX = "_CAR";

	//Query avec facette sur le constructeur
	public static final String QRY_CAR_FACET = "QRY_CAR_FACET";

	/**
	 * Initialise les facettes de l'objet de test Car.
	 * Les Definitions : FacetedQueryDefinition et FacetDefinition doivent avoir été enregistrées.
	 */
	public static void initCarFacet() {
		final DtDefinition carDefinition = DtObjectUtil.findDtDefinition(Car.class);

		//On ajoute les types de requêtes à facettes par index
		final FacetedQueryDefinition carQueryDefinition = createCarQueryDefinitionWithFacets(carDefinition);
		Home.getDefinitionSpace().put(carQueryDefinition);
	}

	/*
	 * Création d'un type derequête avec les facettes suivantes
	 * - Par marque (keyword)
	 * - Par année  (range)
	 * - Par kilométrage (range)
	 * - Par term sur description
	 */
	private static FacetedQueryDefinition createCarQueryDefinitionWithFacets(final DtDefinition carDefinition) {
		final List<FacetDefinition> facetDefinitions = new ArrayList<>();

		//Facette par description
		final DtField descriptionDtField = carDefinition.getField("DESCRIPTION");
		FacetDefinition facetDefinition;
		facetDefinition = FacetDefinition.createFacetDefinitionByTerm(FCT_DESCRIPTION_CAR, descriptionDtField, new MessageText("description", null));
		Home.getDefinitionSpace().put(facetDefinition);
		facetDefinitions.add(facetDefinition);

		//Facette par constructeur
		final DtField makeDtField = carDefinition.getField("MAKE");
		facetDefinition = FacetDefinition.createFacetDefinitionByTerm(FCT_MAKE_CAR, makeDtField, new MessageText("Par constructeur", null));
		Home.getDefinitionSpace().put(facetDefinition);
		facetDefinitions.add(facetDefinition);

		//Facette par range de date
		final DtField yearDtField = carDefinition.getField("YEAR");
		facetDefinition = new FacetDefinitionByRangeBuilder(FCT_YEAR_CAR, yearDtField, new MessageText("Par date", null))
				.addFacetValue("YEAR:[* TO 2000]", "avant 2000")
				.addFacetValue("YEAR:[2000 TO 2005]", "2000-2005")
				.addFacetValue("YEAR:[2005 TO *]", "après 2005")
				.build();

		Home.getDefinitionSpace().put(facetDefinition);
		facetDefinitions.add(facetDefinition);

		final Domain criteriaDomain = descriptionDtField.getDomain();

		return new FacetedQueryDefinition(QRY_CAR_FACET, carDefinition, facetDefinitions, criteriaDomain, (Class<? extends ListFilterBuilder<?>>) DefaultListFilterBuilder.class, DefaultListFilterBuilder.DEFAULT_QUERY);
	}
}
