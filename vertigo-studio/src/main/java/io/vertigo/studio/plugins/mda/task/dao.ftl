<#import "macro_ao.ftl" as lib>
package ${dao.packageName};

<#if !dao.taskDefinitions.empty >
import javax.inject.Inject;

import io.vertigo.kernel.Home;
<#if dao.options >
import io.vertigo.kernel.lang.Option;
</#if>
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.impl.persistence.util.DAOBroker;
<#else>
import javax.inject.Inject;

import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.impl.persistence.util.DAOBroker;
</#if>

/**
 * DAO : Accès à un object (DTO, DTC). 
 * ${dao.classSimpleName}
 */
public final class ${dao.classSimpleName} extends DAOBroker<${dao.dtClassCanonicalName}, ${dao.pkFieldType}> {
	<#if !dao.taskDefinitions.empty>
	<@lib.generateHeader dao.taskDefinitions/>  

	/**
	 * Contructeur.
	 * @param persistenceManager Manager de persistance
	 * @param workManager Manager de Work
	 */
	@Inject
	public ${dao.classSimpleName}(final PersistenceManager persistenceManager, final WorkManager workManager) {
		super(${dao.dtClassCanonicalName}.class, persistenceManager);
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
	}
	<@lib.generateBody dao.taskDefinitions/>  
   	<#else>

	/**
	 * Contructeur.
	 * @param persistenceManager Manager de persistance
	 */
	@Inject
	public ${dao.classSimpleName}(final PersistenceManager persistenceManager) {
		super(${dao.dtClassCanonicalName}.class, persistenceManager);
	}
    </#if>
}
