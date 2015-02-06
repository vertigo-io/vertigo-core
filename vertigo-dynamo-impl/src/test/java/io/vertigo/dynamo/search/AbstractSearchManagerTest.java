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
import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.search.model.SearchQueryBuilder;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
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
	public static final String FCT_CAR_SUFFIX = "_CAR";

	//Query sans facette
	public static final String QRY_CAR = "QRY_CAR";

	//Query avec facette sur le constructeur
	public static final String QRY_CAR_FACET = "QRY_CAR_FACET";

	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	/** Manager de recherche. */
	@Inject
	protected SearchManager searchManager;

	/** IndexDefinition. */
	protected SearchIndexDefinition carIndexDefinition;
	private FacetedQueryDefinition carQueryDefinition;
	private FacetedQueryDefinition carFacetQueryDefinition;
	private CarDataBase carDataBase;

	private String facetSuffix;

	/**
	 * Initialise l'index.
	 * @param indexName Nom de l'index
	 */
	protected final void init(final String indexName) {
		//On construit la BDD des voitures
		carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		facetSuffix = FCT_CAR_SUFFIX;
		carIndexDefinition = Home.getDefinitionSpace().resolve(indexName, SearchIndexDefinition.class);
		carQueryDefinition = Home.getDefinitionSpace().resolve(QRY_CAR, FacetedQueryDefinition.class);
		carFacetQueryDefinition = Home.getDefinitionSpace().resolve(QRY_CAR_FACET, FacetedQueryDefinition.class);
		clean(carIndexDefinition);
	}

	@BeforeClass
	public static void doBeforeClass() throws Exception {
		//We must remove data dir in index, in order to support versions updates when testing on PIC
		final URL esDataURL = Thread.currentThread().getContextClassLoader().getResource("io/vertigo/dynamo/search/serverelastic/data");
		final File esData = new File(URLDecoder.decode(esDataURL.getFile(), "UTF-8"));
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

		size = query("DESCRIPTION:panoRAmique");
		Assert.assertEquals(carDataBase.containsDescription("panoramique"), size);

		size = query("DESCRIPTION:clim");
		Assert.assertEquals(carDataBase.containsDescription("clim"), size);

		size = query("DESCRIPTION:avenir");
		Assert.assertEquals(carDataBase.containsDescription("avenir"), size);

		size = query("DESCRIPTION:l'avenir");
		Assert.assertEquals(carDataBase.containsDescription("l'avenir"), size);
	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSortedQuery() {
		index(false);
		Car firstCar;

		firstCar = doQueryAndGetFirst("*:*", "MAKE", true);
		Assert.assertEquals("Audi", firstCar.getMake());

		firstCar = doQueryAndGetFirst("*:*", "MAKE", false);
		Assert.assertEquals("Volkswagen", firstCar.getMake());

		firstCar = doQueryAndGetFirst("*:*", "YEAR", true);
		Assert.assertEquals(1998, firstCar.getYear().intValue());

		firstCar = doQueryAndGetFirst("*:*", "YEAR", false);
		Assert.assertEquals(2010, firstCar.getYear().intValue());

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
				.withFacetStrategy(carFacetQueryDefinition)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery);
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
				.withFacetStrategy(carFacetQueryDefinition)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery);

		//on applique une facette
		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_YEAR" + facetSuffix, "avant", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> resultFiltered = searchManager.loadList(carIndexDefinition, searchQuery2);
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
		final FacetedQuery previousQuery = result.getFacetedQuery();
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
				.withFacetStrategy(carFacetQueryDefinition)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery);
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
				.withFacetStrategy(carFacetQueryDefinition)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery);
		Assert.assertEquals(carDataBase.getByMake("peugeot").size(), getFacetValueCount("FCT_MAKE" + facetSuffix, "peugeot", result));
		//on applique une facette
		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_MAKE" + facetSuffix, "peugeot", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> resultFiltered = searchManager.loadList(carIndexDefinition, searchQuery2);
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
				.withFacetStrategy(carFacetQueryDefinition)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery);
		//logResult(result);
		//on applique une facette
		Assert.assertEquals(peugeotCars.size(), getFacetValueCount("FCT_MAKE" + facetSuffix, "peugeot", result));
		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_MAKE" + facetSuffix, "peugeot", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result1 = searchManager.loadList(carIndexDefinition, searchQuery2);
		Assert.assertEquals(peugeotCars.size(), (int) result1.getCount());
		logResult(result1);
		//on applique une autre facette
		Assert.assertEquals(peugeotContainsCuirCount, getFacetValueCount("FCT_DESCRIPTION" + facetSuffix, "cuir", result1));
		final SearchQuery searchQuery3 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_DESCRIPTION" + facetSuffix, "cuir", result1))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result2 = searchManager.loadList(carIndexDefinition, searchQuery3);
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
				.withFacetStrategy(carFacetQueryDefinition)
				.build();
		final FacetedQueryResult<Car, SearchQuery> result = searchManager.loadList(carIndexDefinition, searchQuery);
		logResult(result);
		//on applique une facette
		Assert.assertEquals(car2000To2005Count, getFacetValueCount("FCT_YEAR" + facetSuffix, "2000-2005", result));

		final SearchQuery searchQuery2 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_YEAR" + facetSuffix, "2000-2005", result))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result2 = searchManager.loadList(carIndexDefinition, searchQuery2);

		Assert.assertEquals(car2000To2005Count, result2.getCount());
		logResult(result2);
		//on applique une autre facette
		Assert.assertEquals(peugeot2000To2005Count, getFacetValueCount("FCT_MAKE" + facetSuffix, "peugeot", result2));

		final SearchQuery searchQuery3 = new SearchQueryBuilder("*:*")
				.withFacetStrategy(createFacetQuery("FCT_MAKE" + facetSuffix, "peugeot", result2))
				.build();
		final FacetedQueryResult<Car, SearchQuery> result1 = searchManager.loadList(carIndexDefinition, searchQuery3);
		Assert.assertEquals(peugeot2000To2005Count, (int) result1.getCount());
		logResult(result1);
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

	private long containsDescription(final List<Car> cars, final String word) {
		long count = 0;
		for (final Car car : cars) {
			if (car.getDescription().toLowerCase().contains(word)) {
				count++;
			}
		}
		return count;
	}

	private long before(final List<Car> cars, final int year) {
		long count = 0;
		for (final Car car : cars) {
			if (car.getYear() <= year) {
				count++;
			}
		}
		return count;
	}

	private long query(final String query) {
		return doQuery(query);

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
				indexes.add(SearchIndex.createIndex(carIndexDefinition, createURI(car), car, car));
			}
			searchManager.putAll(carIndexDefinition, indexes);
		} else {
			//Indexation unitaire
			//Indexation des cars de la base
			for (final Car car : carDataBase) {
				final SearchIndex<Car, Car> index = SearchIndex.createIndex(carIndexDefinition, createURI(car), car, car);
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

	private long doQuery(final String query) {
		//recherche
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.withFacetStrategy(carQueryDefinition)
				.build();

		return doQuery(searchQuery).getCount();
	}

	private <D extends DtObject> D doQueryAndGetFirst(final String query, final String sortField, final boolean sortAsc) {
		//recherche
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.withSortStrategy(carIndexDefinition.getIndexDtDefinition().getField(sortField), sortAsc)
				.withFacetStrategy(carQueryDefinition)
				.build();

		final DtList<D> dtList = (DtList<D>) doQuery(searchQuery).getDtList();
		Assert.assertFalse("Result list was empty", dtList.isEmpty());
		return dtList.get(0);
	}

	private FacetedQueryResult<DtObject, SearchQuery> doQuery(final SearchQuery searchQuery) {
		return searchManager.loadList(carIndexDefinition, searchQuery);
	}

	private FacetedQueryResult<Car, SearchQuery> doFacetQuery(final String query) {
		final SearchQuery searchQuery = new SearchQueryBuilder(query)
				.withFacetStrategy(carFacetQueryDefinition)
				.build();
		return searchManager.loadList(carIndexDefinition, searchQuery);
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
