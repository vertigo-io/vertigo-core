<#import "macro_ao.ftl" as lib>
package ${dao.packageName};

import javax.inject.Inject;

<#if !dao.taskDefinitions.empty >
import io.vertigo.core.Home;
<#if dao.options >
import io.vertigo.lang.Option;
</#if>
<#if dao.hasSearchBehavior()>
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.search.model.SearchQueryBuilder;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import ${dao.indexDtClassCanonicalName};
</#if>
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
</#if>
<#if dao.dtSubject>
import io.vertigo.dynamo.domain.model.URI;
</#if>
import io.vertigo.dynamo.impl.persistence.util.DAOBroker;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.task.TaskManager;
import ${dao.dtClassCanonicalName};

/**
 * DAO : Accès à un object (DTO, DTC). 
 * ${dao.classSimpleName}
 */
public final class ${dao.classSimpleName} extends DAOBroker<${dao.dtClassSimpleName}, ${dao.pkFieldType}> {
	<#if dao.dtSubject && dao.hasSearchBehavior()>
	private final SearchManager searchManager;
	</#if>
	<#if !dao.taskDefinitions.empty>
	<@lib.generateHeader dao.taskDefinitions/>  
	</#if>
	 
	/**
	 * Contructeur.
	 * @param persistenceManager Manager de persistance
	 * @param taskManager Manager de Task
	 <#if dao.dtSubject && dao.hasSearchBehavior()>
	 * @param searchManager Manager de Search
	 </#if>
	 */
	@Inject
	public ${dao.classSimpleName}(final PersistenceManager persistenceManager, final TaskManager taskManager<#if dao.dtSubject && dao.hasSearchBehavior()>, final SearchManager searchManager</#if>) {
		super(${dao.dtClassSimpleName}.class, persistenceManager, taskManager);
		<#if dao.dtSubject && dao.hasSearchBehavior()>
		this.searchManager = searchManager;
		</#if>
	}
	
	<#if dao.dtSubject>
	/**
	 * Indique que le subject associé à cette uri va être modifié.
	 * Techniquement cela interdit les opérations d'ecriture en concurrence 
	 * et envoie un évenement de modification du subject (à la fin de transaction eventuellement) 
	 * @param uri URI du subject modifié
	 */
	 public void workOnSubject(final URI<${dao.dtClassSimpleName}> uri) {
		broker.workOn(uri);
	}

	/**
	 * Indique que le subject associé à cet id va être modifié.
	 * Techniquement cela interdit les opérations d'ecriture en concurrence 
	 * et envoie un évenement de modification du subject (à la fin de transaction eventuellement) 
	 * @param id Clé du subject modifié
	 */
	 public void workOnSubject(final ${dao.pkFieldType} id) {
		workOnSubject(createDtObjectURI(id));
	}
	
	</#if>
    <#if dao.dtSubject && dao.hasSearchBehavior()>
    
    <#list dao.facetedQueryDefinitions as facetedQueryDefinition>
    /**
	 * Création d'une SearchQuery de type : ${facetedQueryDefinition.simpleName}.
	 * @param query Mots clés de recherche
	 * @param listFilters Liste des filtres à appliquer (notament les facettes sélectionnées)
	 * @return SearchQueryBuilder pour ce type de recherche
	 */
	public SearchQueryBuilder createSearchQueryBuilder${facetedQueryDefinition.simpleName}(final String query, final ListFilter... listFilters) {
		final FacetedQueryDefinition facetedQueryDefinition = Home.getDefinitionSpace().resolve("${facetedQueryDefinition.urn}", FacetedQueryDefinition.class);
		return new SearchQueryBuilder(query).withFacetStrategy(facetedQueryDefinition, listFilters);
	}
	</#list>
    
	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param listState Etat de la liste (tri et pagination)
	 * @return Résultat correspondant à la requête (de type ${dao.indexDtClassSimpleName}) 
	 */
	public FacetedQueryResult<${dao.indexDtClassSimpleName}, SearchQuery> loadList(final SearchQuery searchQuery, final DtListState listState) {
		final SearchIndexDefinition indexDefinition = searchManager.findIndexDefinitionBySubject(${dao.dtClassSimpleName}.class);
		return searchManager.loadList(indexDefinition, searchQuery, listState);
	}
	
    </#if>
	<#if !dao.taskDefinitions.empty>
	<@lib.generateBody dao.taskDefinitions/>  
    </#if>

}
