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
package io.vertigo.dynamo.impl.collections;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.facet.model.FacetFactory;

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
	//private final Option<IndexPlugin> indexPlugin;

	private final FacetFactory facetFactory;
	private final DtListProcessor listProcessor;

	/**
	 * Constructeur.
	 */
	@Inject
	public CollectionsManagerImpl(final Option<IndexPlugin> indexPlugin) {
		Assertion.checkNotNull(indexPlugin);
		//---------------------------------------------------------------------
		//	this.indexPlugin = indexPlugin;
		facetFactory = new FacetFactory(this);
		listProcessor = new DtListProcessorImpl(indexPlugin);
		//		Assertion.notNull(masterDataManager);
		//		Assertion.notNull(persistenceManager);
		//---------------------------------------------------------------------
		Home.getDefinitionSpace().register(FacetDefinition.class);
		Home.getDefinitionSpace().register(FacetedQueryDefinition.class);
		//this.masterDataManager = masterDataManager;
		//persistenceManagerManager = persistenceManager;
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
			filteredDtList = this.createDtListProcessor()//
					.filter(listFilter)//
					.apply(filteredDtList);
		}
		return filteredDtList;
	}

	/** {@inheritDoc} */
	public DtListProcessor createDtListProcessor() {
		return listProcessor;
	}
}
