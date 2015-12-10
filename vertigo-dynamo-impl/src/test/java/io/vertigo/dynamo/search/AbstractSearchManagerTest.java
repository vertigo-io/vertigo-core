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
package io.vertigo.dynamo.search;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.search.model.SearchQueryBuilder;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;
import io.vertigo.dynamock.facet.CarFacetInitializer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author  npiedeloup
 */
public abstract class AbstractSearchManagerTest extends AbstractTestCaseJU4 {
	private static final List<ListFilter> EMPTY_LIST_FILTERS = Collections.emptyList();

	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	/** Manager de recherche. */
	@Inject
	protected SearchManager searchManager;

	/** IndexDefinition. */
	protected SearchIndexDefinition carIndexDefinition;
	private FacetedQueryDefinition carFacetQueryDefinition;
	private FacetDefinition makeFacetDefinition;
	private FacetDefinition yearFacetDefinition;
	private CarDataBase carDataBase;

	private String facetSuffix;

	/**
	 * Initialise l'index.
	 * @param indexName Nom de l'index
	 */
	protected final void init(final String indexName) {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		//On construit la BDD des voitures
		carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		facetSuffix = CarFacetInitializer.FCT_CAR_SUFFIX;
		makeFacetDefinition = definitionSpace.resolve(CarFacetInitializer.FCT_MAKE_CAR, FacetDefinition.class);
		yearFacetDefinition = definitionSpace.resolve(CarFacetInitializer.FCT_YEAR_CAR, FacetDefinition.class);
		carIndexDefinition = definitionSpace.resolve(indexName, SearchIndexDefinition.class);
		carFacetQueryDefinition = definitionSpace.resolve(CarFacetInitializer.QRY_CAR_FACET, FacetedQueryDefinition.class);
		clean(carIndexDefinition);
	}

	@BeforeClass
	public static void doBeforeClass() throws Exception {
		//We must remove data dir in index, in order to support versions updates when testing on PIC
		final URL esDataURL = Thread.currentThread().getContextClassLoader().getResource("io/vertigo/dynamo/search/serverelastic");
		final File esData = new File(URLDecoder.decode(esDataURL.getFile() + "/data", "UTF-8"));
		if (esData.exists() && esData.isDirectory()) {
			recursiveDelete(esData);
		}
	}

	private static void recursiveDelete(final File file) throws IOException {

		if (file.isDirectory()) {
			//list all the directory contents
			for (final File subFile : file.listFiles()) {
				//recursive delete
				recursiveDelete(subFile);
			}
			if (!file.delete()) {
				System.err.println("Can't delete directory : " + file.getAbsolutePath());
			}
		} else {
			//if file, then delete it
			if (!file.delete()) {
				System.err.println("Can't delete file : " + file.getAbsolutePath());
			}
		}
	}

	/**
	 * @param indexDefinition Definition de l'index
	 */
	private void clean(final SearchIndexDefinition indexDefinition) {
		final ListFilter removeQuery = new ListFilter("*:*");
		searchManager.removeAll(indexDefinition, removeQuery);
	}

	/**
	 * Test de création nettoyage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testClean() {
		clean(carIndexDefinition);
	}

	/**
	 * Test de création de n enregistrements dans l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndex() {
		index(false);
		index(true);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexQuery() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexAllQuery() {
		index(true);
		waitIndexation();
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testQuery() {
		index(false);
		waitIndexation();
		long size;
		size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);

		size = query("MAKE:Peugeot"); //Les constructeur sont des mots clés donc sensible à la casse
		Assert.assertEquals(carDataBase.getByMake("peugeot").size(), (int) size);

		size = query("MAKE:peugeot"); //Les constructeur sont des mots clés donc sensible à la casse
		Assert.assertEquals(0L, size);

		size = query("MAKE:Vol*"); //On compte les volkswagen
		Assert.assertEquals(carDataBase.getByMake("volkswagen").size(), (int) size);

		size = query("MAKE:vol*"); //On compte les volkswagen
		Assert.assertEquals(0L, (int) size); //Les constructeur sont des mots clés donc sensible à la casse (y compris en wildcard)

		size = query("YEAR:[* TO 2005]"); //On compte les véhicules avant 2005
		Assert.assertEquals(carDataBase.before(2005), size);

		size = query("DESCRIPTION:panoRAmique");//La description est un text insenssible à la casse
		Assert.assertEquals(carDataBase.containsDescription("panoramique"), size);

		size = query("DESCRIPTION:panoRAmi*");//La description est un text insenssible à la casse (y compris en wildcard)
		Assert.assertEquals(carDataBase.containsDescription("panoramique"), size);

		size = query("DESCRIPTION:clim");
		Assert.assertEquals(carDataBase.containsDescription("clim"), size);

		size = query("DESCRIPTION:avenir");
		Assert.assertEquals(carDataBase.containsDescription("avenir"), size);

		size = query("DESCRIPTION:l'avenir");
		Assert.assertEquals(carDataBase.containsDescription("l'avenir"), size);
	}

	/**
	 * Test de requétage de l'index description : insenssible à la casse et aux accents.
	 */
	@Test
	public void testInsensitivityQuery() {
		index(false);
		waitIndexation();

		final long databaseResult = carDataBase.containsDescription("sieges") + carDataBase.containsDescription("sièges");
		long size;
		size = query("DESCRIPTION:sieges");
		Assert.assertEquals(databaseResult, size);
		size = query("DESCRIPTION:Sieges");
		Assert.assertEquals(databaseResult, size);
		size = query("DESCRIPTION:sièges");
		Assert.assertEquals(databaseResult, size);
		size = query("DESCRIPTION:Sièges");
		Assert.assertEquals(databaseResult, size);

		//y compris en wildcard
		size = query("DESCRIPTION:sièg*");
		Assert.assertEquals(databaseResult, size);
		size = query("DESCRIPTION:Sièg*");
		Assert.assertEquals(databaseResult, size);
		size = query("DESCRIPTION:sieg*");
		Assert.assertEquals(databaseResult, size);
		size = query("DESCRIPTION:Sieg*");
		Assert.assertEquals(databaseResult, size);

	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSortedQuery() {
		index(false);
		Car firstCar;

		firstCar = doQueryAndGetFirst("*:*", "MAKE", false);
		Assert.assertEquals("Audi", firstCar.getMake());

		firstCar = doQueryAndGetFirst("*:*", "MAKE", true);
		Assert.assertEquals("Volkswagen", firstCar.getMake());

		firstCar = doQueryAndGetFirst("*:*", "YEAR", false);
		Assert.assertEquals(1998, firstCar.getYear().intValue());

		firstCar = doQueryAndGetFirst("*:*", "YEAR", true);
		Assert.assertEquals(2010, firstCar.getYear().intValue());
	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testPaginatedQuery() {
		index(false);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*").build();
		final DtList<Car> dtListFull = doQuery(searchQuery, null).getDtList();
		final DtList<Car> dtList1 = doQuery(searchQuery, new DtListState(4, 0, null, null)).getDtList();
		final DtList<Car> dtList2 = doQuery(searchQuery, new DtListState(4, 4, null, null)).getDtList();
		final DtList<Car> dtList3 = doQuery(searchQuery, new DtListState(4, 2 * 4, null, null)).getDtList();

		Assert.assertEquals(4, dtList1.size());
		Assert.assertEquals(4, dtList2.size());
		Assert.assertEquals(carDataBase.size() - 2 * 4, dtList3.size()); //9 elements

		Assert.assertEquals(dtListFull.get(0).getId(), dtList1.get(0).getId());
		Assert.assertEquals(dtListFull.get(3).getId(), dtList1.get(dtList1.size() - 1).getId());
		Assert.assertEquals(dtListFull.get(4).getId(), dtList2.get(0).getId());
		Assert.assertEquals(dtListFull.get(7).getId(), dtList2.get(dtList2.size() - 1).getId());
		Assert.assertEquals(dtListFull.get(8).getId(), dtList3.get(0).getId());
		Assert.assertEquals(dtListFull.get(dtListFull.size() - 1).getId(), dtList3.get(dtList3.size() - 1).getId());

	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testFacetQueryByRange() {
		index(false);
		final FacetedQueryResult<Car, SearchQuery> result = facetQuery("*:*");
		testFacetResultByRange(result);
	}

	private void testFacetResultByRange(final FacetedQueryResult<Car, ?> result) {
		Assert.assertEquals(carDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assert.assertEquals(3, result.getFacets().size());

		//On recherche la facette date
		final Facet yearFacet = getFacetByName(result, "FCT_YEAR" + facetSuffix);
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

		//on vérifie l'ordre
		final List<FacetValue> facetValueDefinition = yearFacet.getDefinition().getFacetRanges();
		final List<FacetValue> facetValueResult = new ArrayList<>(yearFacet.getFacetValues().keySet());
		Assert.assertEquals(facetValueDefinition, facetValueResult); //equals vérifie aussi l'ordre
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testFacetQueryByTerm() {
		index(false);
		waitIndexation();
		final FacetedQueryResult<Car, SearchQuery> result = facetQuery("*:*");
		testFacetResultByTerm(result);
	}

	private void testFacetResultByTerm(final FacetedQueryResult<Car, ?> result) {
		Assert.assertEquals(carDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assert.assertEquals(3, result.getFacets().size());

		//On recherche la facette constructeur
		final Facet makeFacet = getFacetByName(result, "FCT_MAKE" + facetSuffix);
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

		//on vérifie l'ordre
		int lastCount = Integer.MAX_VALUE;
		for (final Entry<FacetValue, Long> entry : makeFacet.getFacetValues().entrySet()) {
			Assert.assertTrue("Ordre des facettes par 'count' non respecté", entry.getValue().intValue() <= lastCount);
			lastCount = entry.getValue().intValue();
		}
	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSecurityQuery() {
		index(false);
		waitIndexation();
		long size;
		size = query("*:*", "+YEAR:[ 2005 TO * ]");
		Assert.assertEquals(carDataBase.size() - carDataBase.before(2005), size);

		size = query("MAKE:Peugeot", "+YEAR:[2005 TO * ]"); //Les constructeur sont des mots clés donc sensible à la casse
		Assert.assertEquals(0L, (int) size);

		size = query("MAKE:Vol*", "+YEAR:[2005 TO *]"); //On compte les volkswagen
		Assert.assertEquals(carDataBase.getByMake("volkswagen").size(), (int) size);

		size = query("YEAR:[* TO 2005]", "+YEAR:[2005 TO *]"); //On compte les véhicules avant 2005
		Assert.assertEquals(0L, size);

		size = query("DESCRIPTION:siège", "+YEAR:[2005 TO *]");//La description est un text insenssible à la casse
		Assert.assertEquals(2L, size);

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
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemove() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
		//On en supprime 2
		remove(2);
		waitIndexation();
		final long resize = query("*:*");
		Assert.assertEquals(carDataBase.size() - 2, resize);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveByQuery() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
		//on compte les Peugeots
		int nbPeugeot = 0;
		for (final Car car : carDataBase) {
			if ("Peugeot".equals(car.getMake())) {
				nbPeugeot++;
			}
		}
		//On supprime toute les Peugeots
		remove("MAKE:Peugeot");
		waitIndexation();
		final long resize = query("*:*");
		Assert.assertEquals(carDataBase.size() - nbPeugeot, resize);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveAll() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
		//On supprime tout
		remove("*:*");
		final long resize = query("*:*");
		Assert.assertEquals(0L, resize);
	}

	/**
	 * Test le facettage par range d'une liste.
	 */
	@Test
	public void testFacetListByRange() {
		index(true);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetStrategy(carFacetQueryDefinition, EMPTY_LIST_FILTERS)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);
		testFacetResultByRange(result);
	}

	/**
	 * Test le facettage par range d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByRange() {
		index(true);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetStrategy(carFacetQueryDefinition, EMPTY_LIST_FILTERS)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);

		//on applique une facette
		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_YEAR" + facetSuffix, "avant", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> resultFiltered = searchManager.loadList(carIndexDefinition, searchQuery2, null);
		Assert.assertEquals(carDataBase.before(2000), resultFiltered.getCount());
	}

	private static FacetedQuery createFacetQuery(final String facetName, final String facetValueLabel, final FacetedQueryResult<Car, ?> result) {
		FacetValue facetValue = null; //pb d'initialisation, et assert.notNull ne suffit pas
		final Facet facet = getFacetByName(result, facetName);
		for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().contains(facetValueLabel)) {
				facetValue = entry.getKey();
				break;
			}
		}
		if (facetValue == null) {
			throw new IllegalArgumentException("Pas de FacetValue contenant " + facetValueLabel + " dans la facette " + facetName);
		}
		final FacetedQuery previousQuery = result.getFacetedQuery().get();
		final List<ListFilter> queryFilters = new ArrayList<>(previousQuery.getListFilters());
		queryFilters.add(facetValue.getListFilter());
		return new FacetedQuery(previousQuery.getDefinition(), queryFilters);
	}

	private static long getFacetValueCount(final String facetName, final String facetValueLabel, final FacetedQueryResult<Car, ?> result) {
		final Facet facet = getFacetByName(result, facetName);
		for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().contains(facetValueLabel)) {
				return entry.getValue();
			}
		}
		return 0;
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testFacetListByTerm() {
		index(true);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetStrategy(carFacetQueryDefinition, EMPTY_LIST_FILTERS)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);
		testFacetResultByTerm(result);
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByTerm() {
		index(true);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetStrategy(carFacetQueryDefinition, EMPTY_LIST_FILTERS)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);
		Assert.assertEquals(carDataBase.getByMake("peugeot").size(), getFacetValueCount("FCT_MAKE" + facetSuffix, "peugeot", result));
		//on applique une facette
		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_MAKE" + facetSuffix, "peugeot", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> resultFiltered = searchManager.loadList(carIndexDefinition, searchQuery2, null);
		Assert.assertEquals(carDataBase.getByMake("peugeot").size(), (int) resultFiltered.getCount());
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par deux facettes.
	 */
	@Test
	public void testFilterFacetListByTwoTerms() {
		index(true);
		final List<Car> peugeotCars = carDataBase.getByMake("peugeot");
		final long peugeotContainsCuirCount = containsDescription(peugeotCars, "cuir");
		//final long peugeotContainsSiegCount = carDataBase.containsDescription("cuir");

		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetStrategy(carFacetQueryDefinition, EMPTY_LIST_FILTERS)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);
		//logResult(result);
		//on applique une facette
		Assert.assertEquals(peugeotCars.size(), getFacetValueCount("FCT_MAKE" + facetSuffix, "peugeot", result));
		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_MAKE" + facetSuffix, "peugeot", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result1 = searchManager.loadList(carIndexDefinition, searchQuery2, null);
		Assert.assertEquals(peugeotCars.size(), (int) result1.getCount());
		logResult(result1);
		//on applique une autre facette
		Assert.assertEquals(peugeotContainsCuirCount, getFacetValueCount("FCT_DESCRIPTION" + facetSuffix, "cuir", result1));
		final SearchQuery searchQuery3 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_DESCRIPTION" + facetSuffix, "cuir", result1))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result2 = searchManager.loadList(carIndexDefinition, searchQuery3, null);
		Assert.assertEquals(peugeotContainsCuirCount, (int) result2.getCount());
		logResult(result2);
	}

	/**
	 * Test le facettage par range d'une liste.
	 * Et le filtrage par deux facettes term et range.
	 */
	@Test
	public void testFilterFacetListByRangeAndTerm() {
		index(true);
		final long car2000To2005Count = carDataBase.before(2005) - carDataBase.before(2000);
		final List<Car> peugeotCars = carDataBase.getByMake("peugeot");
		final long peugeot2000To2005Count = before(peugeotCars, 2005) - before(peugeotCars, 2000);

		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetStrategy(carFacetQueryDefinition, EMPTY_LIST_FILTERS)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);
		logResult(result);
		//on applique une facette
		Assert.assertEquals(car2000To2005Count, getFacetValueCount("FCT_YEAR" + facetSuffix, "2000-2005", result));

		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_YEAR" + facetSuffix, "2000-2005", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result2 = searchManager.loadList(carIndexDefinition, searchQuery2, null);

		Assert.assertEquals(car2000To2005Count, result2.getCount());
		logResult(result2);
		//on applique une autre facette
		Assert.assertEquals(peugeot2000To2005Count, getFacetValueCount("FCT_MAKE" + facetSuffix, "peugeot", result2));

		final SearchQuery searchQuery3 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_MAKE" + facetSuffix, "peugeot", result2))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result1 = searchManager.loadList(carIndexDefinition, searchQuery3, null);
		Assert.assertEquals(peugeot2000To2005Count, (int) result1.getCount());
		logResult(result1);
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testClusterByFacetTerm() {
		index(true);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetClustering(makeFacetDefinition)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);

		//On vérifie qu'il existe une valeur pour chaque marques et que le nombre d'occurrences est correct
		final Map<String, List<Car>> databaseCluster = new HashMap<>();
		for (final Car car : carDataBase) {
			List<Car> carsByMake = databaseCluster.get(car.getMake().toLowerCase());
			if (carsByMake == null) {
				carsByMake = new ArrayList<>();
				databaseCluster.put(car.getMake().toLowerCase(), carsByMake);
			}
			carsByMake.add(car);
		}
		int previousCount = Integer.MAX_VALUE;
		Assert.assertEquals(databaseCluster.size(), result.getClusters().size());
		for (final Entry<FacetValue, DtList<Car>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase();
			final int searchFacetCount = entry.getValue().size();
			final List<Car> carsByMake = databaseCluster.get(searchFacetLabel);
			Assert.assertEquals(carsByMake.size(), searchFacetCount);
			Assert.assertTrue("Group order invalid", previousCount >= searchFacetCount);
			previousCount = searchFacetCount;
			for (final Car car : entry.getValue()) {
				Assert.assertEquals(searchFacetLabel, car.getMake().toLowerCase());
			}
		}
	}

	private enum YearCluster {
		before2000("avant 2000"),
		between2000and2005("2000-2005"),
		after2005("apres 2005");

		private final String label;

		YearCluster(final String label) {
			this.label = label;
		}

		String getLabel() {
			return label;
		}
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testClusterByFacetRange() {
		index(true);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetClustering(yearFacetDefinition) // "avant 2000", "2000-2005", "après 2005"
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, null);

		//On vérifie qu'il existe une valeur pour chaque marques et que le nombre d'occurrences est correct
		final Map<String, List<Car>> databaseCluster = new HashMap<>();
		databaseCluster.put(YearCluster.before2000.getLabel(), new ArrayList<Car>());
		databaseCluster.put(YearCluster.between2000and2005.getLabel(), new ArrayList<Car>());
		databaseCluster.put(YearCluster.after2005.getLabel(), new ArrayList<Car>());
		for (final Car car : carDataBase) {
			if (car.getYear() < 2000) {
				databaseCluster.get(YearCluster.before2000.getLabel()).add(car);
			} else if (car.getYear() < 2005) {
				databaseCluster.get(YearCluster.between2000and2005.getLabel()).add(car);
			} else {
				databaseCluster.get(YearCluster.after2005.getLabel()).add(car);
			}
		}
		Assert.assertEquals(databaseCluster.size(), result.getClusters().size());
		for (final Entry<FacetValue, DtList<Car>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase();
			final int searchFacetCount = entry.getValue().size();
			final List<Car> carsByYear = databaseCluster.get(searchFacetLabel);
			Assert.assertEquals(carsByYear.size(), searchFacetCount);
			for (final Car car : entry.getValue()) {
				if (car.getYear() < 2000) {
					Assert.assertEquals(searchFacetLabel, YearCluster.before2000.getLabel());
				} else if (car.getYear() < 2005) {
					Assert.assertEquals(searchFacetLabel, YearCluster.between2000and2005.getLabel());
				} else {
					Assert.assertEquals(searchFacetLabel, YearCluster.after2005.getLabel());
				}
			}
		}
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testClusterByFacetRangeVerySmallMaxRows() {
		index(true);
		final SearchQuery searchQuery = new SearchQueryBuilder("*:*")
				.withFacetClustering(yearFacetDefinition) // "avant 2000", "2000-2005", "après 2005"
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery, new DtListState(1, 0, null, null));

		//On vérifie qu'il existe une valeur pour chaque marques et que le nombre d'occurrences est correct
		final Map<String, List<Car>> databaseCluster = new HashMap<>();
		databaseCluster.put(YearCluster.before2000.getLabel(), new ArrayList<Car>());
		databaseCluster.put(YearCluster.between2000and2005.getLabel(), new ArrayList<Car>());
		databaseCluster.put(YearCluster.after2005.getLabel(), new ArrayList<Car>());
		for (final Car car : carDataBase) {
			if (car.getYear() < 2000) {
				databaseCluster.get(YearCluster.before2000.getLabel()).add(car);
			} else if (car.getYear() < 2005) {
				databaseCluster.get(YearCluster.between2000and2005.getLabel()).add(car);
			} else {
				databaseCluster.get(YearCluster.after2005.getLabel()).add(car);
			}
		}
		Assert.assertEquals(databaseCluster.size(), result.getClusters().size());
		for (final Entry<FacetValue, DtList<Car>> entry : result.getClusters().entrySet()) {
			final String searchFacetLabel = entry.getKey().getLabel().getDisplay().toLowerCase();
			final int searchFacetCount = entry.getValue().size();
			final List<Car> carsByYear = databaseCluster.get(searchFacetLabel);
			Assert.assertEquals(carsByYear.size(), searchFacetCount);
			for (final Car car : entry.getValue()) {
				if (car.getYear() < 2000) {
					Assert.assertEquals(searchFacetLabel, YearCluster.before2000.getLabel());
				} else if (car.getYear() < 2005) {
					Assert.assertEquals(searchFacetLabel, YearCluster.between2000and2005.getLabel());
				} else {
					Assert.assertEquals(searchFacetLabel, YearCluster.after2005.getLabel());
				}
			}
		}
	}

	private void logResult(final FacetedQueryResult<Car, SearchQuery> result) {
		log.info("====== " + result.getCount() + " Results");
		for (final Facet facet : result.getFacets()) {
			log.info("\tFacet " + facet.getDefinition().getLabel().getDisplay());
			for (final Entry<FacetValue, Long> facetValue : facet.getFacetValues().entrySet()) {
				log.info("\t\t+ " + facetValue.getKey().getLabel().getDisplay() + " : " + facetValue.getValue());
			}
		}
	}

	private static long containsDescription(final List<Car> cars, final String word) {
		long count = 0;
		for (final Car car : cars) {
			if (car.getDescription().toLowerCase().contains(word)) {
				count++;
			}
		}
		return count;
	}

	private static long before(final List<Car> cars, final int year) {
		long count = 0;
		for (final Car car : cars) {
			if (car.getYear() <= year) {
				count++;
			}
		}
		return count;
	}

	private FacetedQueryResult<Car, SearchQuery> facetQuery(final String query) {
		return doFacetQuery(query);

	}

	private void index(final boolean all) {
		doIndex(all);

	}

	private void remove(final int count) {
		doRemove(count);

	}

	private void remove(final String query) {
		doRemove(query);

	}

	private static URI createURI(final Car car) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
		return new URI(dtDefinition, DtObjectUtil.getId(car));
	}

	private void doIndex(final boolean all) {
		if (all) {
			final List<SearchIndex<Car, Car>> indexes = new ArrayList<>();
			for (final Car car : carDataBase) {
				indexes.add(SearchIndex.createIndex(carIndexDefinition, createURI(car), car));
			}
			searchManager.putAll(carIndexDefinition, indexes);
		} else {
			//Indexation unitaire
			//Indexation des cars de la base
			for (final Car car : carDataBase) {
				final SearchIndex<Car, Car> index = SearchIndex.createIndex(carIndexDefinition, createURI(car), car);
				searchManager.put(carIndexDefinition, index);
			}
		}
		waitIndexation();
	}

	private void doRemove(final int count) {
		//Suppression de n voitures
		for (long id = 0; id < count; id++) {
			searchManager.remove(carIndexDefinition, createURI(id));
		}
	}

	private void doRemove(final String query) {
		final ListFilter removeQuery = new ListFilter(query);
		searchManager.removeAll(carIndexDefinition, removeQuery);
	}

	private long query(final String query) {
		//recherche
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private long query(final String query, final String securityFilter) {
		//recherche
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.withSecurityFilter(new ListFilter(securityFilter))
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private Car doQueryAndGetFirst(final String query, final String sortField, final boolean sortDesc) {
		//recherche
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.build();
		final DtListState listState = new DtListState(null, 0, carIndexDefinition.getIndexDtDefinition().getField(sortField).getName(), sortDesc);
		final DtList<Car> dtList = doQuery(searchQuery, listState).getDtList();
		Assert.assertFalse("Result list was empty", dtList.isEmpty());
		return dtList.get(0);
	}

	private FacetedQueryResult<Car, SearchQuery> doQuery(final SearchQuery searchQuery, final DtListState listState) {
		return searchManager.loadList(carIndexDefinition, searchQuery, listState);
	}

	private FacetedQueryResult<Car, SearchQuery> doFacetQuery(final String query) {
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.withFacetStrategy(carFacetQueryDefinition, EMPTY_LIST_FILTERS)
				.build();
		return searchManager.loadList(carIndexDefinition, searchQuery, null);
	}

	private static URI createURI(final long id) {
		return new URI(DtObjectUtil.findDtDefinition(Car.class), id);
	}

	private static void waitIndexation() {
		try {
			Thread.sleep(1500); //wait index was done
		} catch (final InterruptedException e) {
			//rien
		}
	}
}
