<#import "macro_ao.ftl" as lib>
package ${dao.packageName};

import javax.inject.Inject;

<#if !dao.taskDefinitions.empty >
import io.vertigo.core.Home;
<#if dao.options >
import io.vertigo.lang.Option;
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

/**
 * DAO : Accès à un object (DTO, DTC). 
 * ${dao.classSimpleName}
 */
public final class ${dao.classSimpleName} extends DAOBroker<${dao.dtClassCanonicalName}, ${dao.pkFieldType}> {
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
		super(${dao.dtClassCanonicalName}.class, persistenceManager, taskManager);
		<#if dao.dtSubject && dao.hasSearchBehavior()>
		this.searchManager = searchManager;
		</#if>
	}
	
	<#if dao.dtSubject>
	public void workOnSubject(final URI<${dao.dtClassCanonicalName}> uri) {
		broker.workOn(uri);
	}

	public void workOnSubject(final ${dao.pkFieldType} id) {
		workOnSubject(createDtObjectURI(id));
	}
	
	</#if>
    <#if dao.dtSubject && dao.hasSearchBehavior()>
	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param listState Etat de la liste (tri et pagination)
	 * @return Résultat correspondant à la requête
	 * @param <R> Type de l'objet resultant de la recherche
	 */
	public <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchQuery searchQuery, final DtListState listState) {
		final SearchIndexDefinition indexDefinition = searchManager.findIndexDefinitionBySubject(${dao.dtClassCanonicalName}.class);
		return searchManager.loadList(indexDefinition, searchQuery, listState);
	}
	
    </#if>
	<#if !dao.taskDefinitions.empty>
	<@lib.generateBody dao.taskDefinitions/>  
    </#if>

}
