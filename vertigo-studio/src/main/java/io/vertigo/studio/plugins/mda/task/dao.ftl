<#import "macro_ao.ftl" as lib>
package ${dao.packageName};

import javax.inject.Inject;
<#if dao.hasSearchBehavior()>
import java.util.List;
</#if>
<#if !dao.taskDefinitions.empty || dao.hasSearchBehavior() >
import io.vertigo.app.Home;
</#if>
<#if dao.hasSearchBehavior()>
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.search.model.SearchQueryBuilder;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import ${dao.indexDtClassCanonicalName};
</#if>
<#if !dao.taskDefinitions.empty >
<#if dao.options >
import io.vertigo.lang.Option;
</#if>
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
</#if>
<#if dao.keyConcept>
import io.vertigo.dynamo.domain.model.URI;
</#if>
import io.vertigo.dynamo.impl.store.util.DAOBroker;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.StoreServices;
import io.vertigo.dynamo.task.TaskManager;
import ${dao.dtClassCanonicalName};

/**
 * DAO : Accès à un object (DTO, DTC). 
 * ${dao.classSimpleName}
 */
public final class ${dao.classSimpleName} extends DAOBroker<${dao.dtClassSimpleName}, ${dao.pkFieldType}> implements StoreServices {
	<#if dao.keyConcept && dao.hasSearchBehavior()>
	private final SearchManager searchManager;
	</#if>
	 
	/**
	 * Contructeur.
	 * @param storeManager Manager de persistance
	 * @param taskManager Manager de Task
	 <#if dao.keyConcept && dao.hasSearchBehavior()>
	 * @param searchManager Manager de Search
	 </#if>
	 */
	@Inject
	public ${dao.classSimpleName}(final StoreManager storeManager, final TaskManager taskManager<#if dao.keyConcept && dao.hasSearchBehavior()>, final SearchManager searchManager</#if>) {
		super(${dao.dtClassSimpleName}.class, storeManager, taskManager);
		<#if dao.keyConcept && dao.hasSearchBehavior()>
		this.searchManager = searchManager;
		</#if>
	}
	
	<#if dao.keyConcept>
	/**
	 * Indique que le keyConcept associé à cette uri va être modifié.
	 * Techniquement cela interdit les opérations d'ecriture en concurrence 
	 * et envoie un évenement de modification du keyConcept (à la fin de transaction eventuellement) 
	 * @param uri URI du keyConcept modifié
	 */
	 public void workOnKeyConcept(final URI<${dao.dtClassSimpleName}> uri) {
		dataStore.workOn(uri);
	}

	/**
	 * Indique que le keyConcept associé à cet id va être modifié.
	 * Techniquement cela interdit les opérations d'ecriture en concurrence 
	 * et envoie un évenement de modification du keyConcept (à la fin de transaction eventuellement) 
	 * @param id Clé du keyConcept modifié
	 */
	 public void workOnKeyConcept(final ${dao.pkFieldType} id) {
		workOnKeyConcept(createDtObjectURI(id));
	}
	
	</#if>
    <#if dao.keyConcept && dao.hasSearchBehavior()>
    
    <#list dao.facetedQueryDefinitions as facetedQueryDefinition>
    /**
	 * Création d'une SearchQuery de type : ${facetedQueryDefinition.simpleName}.
	 * @param criteria Critères de recherche
	 * @param listFilters Liste des filtres à appliquer (notament les facettes sélectionnées)
	 * @return SearchQueryBuilder pour ce type de recherche
	 */
	public SearchQueryBuilder createSearchQueryBuilder${facetedQueryDefinition.simpleName}(final ${facetedQueryDefinition.criteriaClassCanonicalName} criteria, final List<ListFilter> listFilters) {
		final FacetedQueryDefinition facetedQueryDefinition = Home.getApp().getDefinitionSpace().resolve("${facetedQueryDefinition.urn}", FacetedQueryDefinition.class);
		final ListFilterBuilder<${facetedQueryDefinition.criteriaClassCanonicalName}> listFilterBuilder = Injector.newInstance(facetedQueryDefinition.getListFilterBuilderClass(), Home.getApp().getComponentSpace());
		final ListFilter criteriaListFilter = listFilterBuilder.withBuildQuery(facetedQueryDefinition.getListFilterBuilderQuery()).withCriteria(criteria).build();
		return new SearchQueryBuilder(criteriaListFilter).withFacetStrategy(facetedQueryDefinition, listFilters);
	}
	</#list>
    
	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param listState Etat de la liste (tri et pagination)
	 * @ret² Résultat correspondant à la requête (de type ${dao.indexDtClassSimpleName}) 
	 */
	public FacetedQueryResult<${dao.indexDtClassSimpleName}, SearchQuery> loadList(final SearchQuery searchQuery, final DtListState listState) {
		final SearchIndexDefinition indexDefinition = searchManager.findIndexDefinitionByKeyConcept(${dao.dtClassSimpleName}.class);
		return searchManager.loadList(indexDefinition, searchQuery, listState);
	}
	
    </#if>
	<#if !dao.taskDefinitions.empty>
	<@lib.generateBody dao.taskDefinitions/>  
    </#if>

}
