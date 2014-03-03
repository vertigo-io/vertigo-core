package io.vertigo.dynamo.collections.facet;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.facet.model.Facet;
import io.vertigo.dynamo.collections.facet.model.FacetValue;
import io.vertigo.dynamo.collections.facet.model.FacetedQuery;
import io.vertigo.dynamo.collections.facet.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;
import io.vertigo.dynamock.facet.CarFacetInitializer;
import io.vertigo.kernel.Home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  npiedeloup
 * @version $Id: FacetManagerTest.java,v 1.3 2014/01/20 17:51:47 pchretien Exp $
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
		Assert.assertEquals(true, yearFacet.getDefinition().isRangeFacet());

		boolean found = false;
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().contains("avant")) {
				found = true;
				Assert.assertEquals(carDataBase.before(2000), entry.getValue().longValue());
			}
		}
		Assert.assertEquals(true, found);
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
		Assert.assertEquals(false, makeFacet.getDefinition().isRangeFacet());

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
		Assert.assertEquals(true, found);
	}

	private Facet getFacetByName(final FacetedQueryResult<Car, ?> result, final String facetName) {
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

	private FacetedQuery addFacetQuery(final String facetName, final String facetValueLabel, final FacetedQueryResult<Car, ?> result) {
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
		final FacetedQuery previousQuery = result.getFacetedQuery();
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
