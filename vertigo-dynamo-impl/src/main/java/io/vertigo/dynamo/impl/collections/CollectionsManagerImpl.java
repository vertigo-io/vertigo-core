package io.vertigo.dynamo.impl.collections;

import io.vertigo.dynamo.Function;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.model.Facet;
import io.vertigo.dynamo.collections.facet.model.FacetedQuery;
import io.vertigo.dynamo.collections.facet.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.facet.model.FacetFactory;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListChainFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.FilterFunction;
import io.vertigo.dynamo.impl.collections.functions.fulltext.FullTextFilterFunction;
import io.vertigo.dynamo.impl.collections.functions.sort.SortFunction;
import io.vertigo.dynamo.impl.collections.functions.sort.SortState;
import io.vertigo.dynamo.impl.collections.functions.sublist.SubListFunction;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
* Implémentation du gestionnaire de la manipulation des collections.
* 
* @author  pchretien
*/
public final class CollectionsManagerImpl implements CollectionsManager {
	//private final MasterDataManager masterDataManager;
	//private final PersistenceManager persistenceManagerManager;
	@Inject
	private Option<IndexPlugin> indexPlugin;

	private final Function<?, ?> identityFunction = new IdentityFunction();

	private final FacetFactory facetFactory;

	/**
	 * Constructeur. 
	 */
	public CollectionsManagerImpl() {
		facetFactory = new FacetFactory(this);
		//		Assertion.notNull(masterDataManager);
		//		Assertion.notNull(persistenceManager);
		//---------------------------------------------------------------------
		//this.masterDataManager = masterDataManager;
		//persistenceManagerManager = persistenceManager;
	}

	// Getteur sur Home car dépendance cyclique entre CollectionsManager et PersistenceManager
	private PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtListFunction<D> createFilter(final String keywords, final int maxRows, final Collection<DtField> searchedFields) {
		Assertion.checkArgument(indexPlugin.isDefined(), "Aucun plugin de collectionsIndexerPlugin déclaré");
		//---------------------------------------------------------------------
		return new FullTextFilterFunction<>(keywords, maxRows, searchedFields, indexPlugin.get());
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtListFunction<D> createSort(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase) {
		final SortState sortState = new SortState(fieldName, desc, nullLast, ignoreCase);
		return new SortFunction<>(sortState, getPersistenceManager());
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtListFunction<D> createFilterByValue(final String fieldName, final Serializable value) {
		final DtListFilter<D> filter = new DtListValueFilter<>(fieldName, value);
		return new FilterFunction<>(filter);
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtListFunction<D> createFilterByTwoValues(final String fieldName1, final Serializable value1, final String fieldName2, final Serializable value2) {
		final DtListFilter<D> filter1 = new DtListValueFilter<>(fieldName1, value1);
		final DtListFilter<D> filter2 = new DtListValueFilter<>(fieldName2, value2);
		final DtListFilter<D> filter = new DtListChainFilter<>(filter1, filter2);
		return new FilterFunction<>(filter);
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtListFunction<D> createFilter(final ListFilter listFilter) {
		final DtListFilter<D> filter = new DtListPatternFilter<>(listFilter.getFilterValue());
		return new FilterFunction<>(filter);
	}

	/** {@inheritDoc} */
	public <D extends DtObject, C extends Comparable<?>> DtListFunction<D> createFilterByRange(final String fieldName, final Option<C> min, final Option<C> max) {
		final DtListFilter<D> filter = new DtListRangeFilter<>(fieldName, min, max, true, true);
		return new FilterFunction<>(filter);
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtListFunction<D> createFilterSubList(final int start, final int end) {
		return new SubListFunction<>(start, end);
	}

	/** {@inheritDoc} */
	public <E> Function<E, E> createIdentity() {
		return (Function<E, E>) identityFunction;
	}

	private static final class IdentityFunction implements Function<Object, Object> {
		/** {@inheritDoc} */
		public Object apply(final Object o) {
			return o;
		}
	}

	//	/** {@inheritDoc} */
	//	public <D extends DtObject> List<Cluster<D>> cluster(final DtList<D> dtc, final DtField TitleDtField, final DtField SummaryDtField) {
	//		final List<Cluster<D>> clusterList = new ArrayList<Cluster<D>>();
	//		final String title;
	//		final String summary;
	//		final String id;
	//
	//		final Document document = new Document();
	//		final List<Document> documents = new ArrayList<Document>();
	//		final SimpleController controller = new SimpleController();
	//		final Map<String, Object> attributes = new HashMap<String, Object>();
	//		attributes.put(AttributeNames.DOCUMENTS, documents);
	//		final ProcessingResult processingResult = controller.process(attributes, ByUrlClusteringAlgorithm.class);
	//		//		for (org.carrot2.core.Cluster c2Cluster : processingResult.getClusters()) {
	//		//			clusterList.add(new Cluster<>() {
	//		//			});
	//		//		}
	//		return clusterList;
	//	}

	/** {@inheritDoc} */
	public <R extends DtObject> FacetedQueryResult<R, DtList<R>> facetList(final DtList<R> dtList, final FacetedQuery facetedQuery) {
		Assertion.checkNotNull(dtList);
		Assertion.checkNotNull(facetedQuery);
		//---------------------------------------------------------------------
		//1- on applique les filtres
		final DtList<R> filteredDtList = filter(dtList, facetedQuery);
		//2- on facette
		final List<Facet> facets = facetFactory.createFacets(facetedQuery.getDefinition(), filteredDtList);
		//TODO 2b- mise en valeur vide
		final Map<R, Map<DtField, String>> highlights = Collections.emptyMap();

		//3- on construit le résultat
		return new FacetedQueryResult<>(facetedQuery, filteredDtList.size(), filteredDtList, facets, highlights, dtList);
	}

	//=========================================================================
	//=======================Filtrage==========================================
	//=========================================================================
	private <D extends DtObject> DtList<D> filter(final DtList<D> dtList, final FacetedQuery facetedQuery) {
		final List<ListFilter> listFilters = facetedQuery.getListFilters();
		DtList<D> filteredDtList = dtList;
		for (final ListFilter listFilter : listFilters) {
			filteredDtList = this.<D> createFilter(listFilter)//;
					.apply(filteredDtList);
		}
		return filteredDtList;
	}
}
