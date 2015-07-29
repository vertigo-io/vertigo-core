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
package io.vertigo.dynamo.collections.facet;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;
import io.vertigo.dynamock.facet.CarFacetInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  npiedeloup
 */
public final class FacetManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private CollectionsManager collectionsManager;
	private FacetedQueryDefinition carFacetQueryDefinition;
	private CarDataBase carDataBase;

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		//On construit la BDD des voitures
		carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		carFacetQueryDefinition = Home.getDefinitionSpace().resolve(CarFacetInitializer.QRY_CAR_FACET, FacetedQueryDefinition.class);
	}

	private void testFacetResultByRange(final FacetedQueryResult<Car, ?> result) {
		Assert.assertEquals(carDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assert.assertEquals(3, result.getFacets().size());

		//On recherche la facette date
		final Facet yearFacet = getFacetByName(result, CarFacetInitializer.FCT_YEAR_CAR);
		Assert.assertNotNull(yearFacet);
		Assert.assertTrue(yearFacet.getDefinition().isRangeFacet());

		boolean found = false;
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().contains("avant")) {
				found = true;
				Assert.assertEquals(carDataBase.before(2000), entry.getValue().longValue());
			}
		}
		Assert.assertTrue(found);
	}

	private void testFacetResultByTerm(final FacetedQueryResult<Car, ?> result) {
		Assert.assertEquals(carDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assert.assertEquals(3, result.getFacets().size());

		//On recherche la facette constructeur
		final Facet makeFacet = getFacetByName(result, CarFacetInitializer.FCT_MAKE_CAR);
		Assert.assertNotNull(makeFacet);
		//On vérifie que l'on est sur le champ Make
		Assert.assertEquals("MAKE", makeFacet.getDefinition().getDtField().getName());
		Assert.assertFalse(makeFacet.getDefinition().isRangeFacet());

		//On vérifie qu'il existe une valeur pour peugeot et que le nombre d'occurrences est correct
		boolean found = false;
		final String make = "peugeot";
		for (final Entry<FacetValue, Long> entry : makeFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().equals(make)) {
				found = true;
				//System.out.println("make" + entry.getKey().getLabel().getDisplay());
				Assert.assertEquals(carDataBase.getByMake(make).size(), entry.getValue().intValue());
			}
		}
		Assert.assertTrue(found);
	}

	private static Facet getFacetByName(final FacetedQueryResult<Car, ?> result, final String facetName) {
		for (final Facet facet : result.getFacets()) {
			if (facetName.equals(facet.getDefinition().getName())) {
				return facet;
			}
		}
		return null;
	}

	/**
	 * Test le facettage par range d'une liste.
	 */
	@Test
	public void testFacetListByRange() {
		final DtList<Car> cars = carDataBase.createList();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		testFacetResultByRange(result);
	}

	/**
	 * Test le facettage par range d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByRange() {
		final DtList<Car> cars = carDataBase.createList();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		//on applique une facette
		final FacetedQuery query = addFacetQuery(CarFacetInitializer.FCT_YEAR_CAR, "avant", result);
		final FacetedQueryResult<Car, DtList<Car>> resultFiltered = collectionsManager.facetList(result.getSource(), query);
		Assert.assertEquals(carDataBase.before(2000), resultFiltered.getCount());
	}

	private static FacetedQuery addFacetQuery(final String facetName, final String facetValueLabel, final FacetedQueryResult<Car, ?> result) {
		FacetValue facetFilter = null; //pb d'initialisation, et assert.notNull ne suffit pas
		final Facet yearFacet = getFacetByName(result, facetName);
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().contains(facetValueLabel)) {
				facetFilter = entry.getKey();
				break;
			}
		}
		if (facetFilter == null) {
			throw new IllegalArgumentException("Pas de FacetValue contenant " + facetValueLabel + " dans la facette " + facetName);
		}
		final FacetedQuery previousQuery = result.getFacetedQuery().get();
		final List<ListFilter> queryFilters = new ArrayList<>(previousQuery.getListFilters());
		queryFilters.add(facetFilter.getListFilter());
		return new FacetedQuery(previousQuery.getDefinition(), queryFilters);
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testFacetListByTerm() {
		final DtList<Car> cars = carDataBase.createList();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		testFacetResultByTerm(result);
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByTerm() {
		final DtList<Car> cars = carDataBase.createList();
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		//on applique une facette
		final FacetedQuery query = addFacetQuery(CarFacetInitializer.FCT_MAKE_CAR, "peugeot", result);
		final FacetedQueryResult<Car, DtList<Car>> resultFiltered = collectionsManager.facetList(result.getSource(), query);
		Assert.assertEquals(carDataBase.getByMake("peugeot").size(), (int) resultFiltered.getCount());
	}

}
