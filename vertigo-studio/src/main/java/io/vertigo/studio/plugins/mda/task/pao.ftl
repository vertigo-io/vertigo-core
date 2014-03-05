<#import "macro_ao.ftl" as lib>
package ${pao.packageName};

import javax.inject.Inject;

<#if pao.options >
import io.vertigo.kernel.lang.Option;
</#if>
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.work.WorkManager;

/**
 * PAO : Accès aux objects du package. 
 * ${pao.classSimpleName}
 */
public final class ${pao.classSimpleName} {
	<@lib.generateHeader pao.taskDefinitions/>  

	/**
	 * Constructeur.
	 * @param workManager Manager des Works
	 */
	@Inject
	public ${pao.classSimpleName}(final WorkManager workManager) {
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
	}
    <@lib.generateBody pao.taskDefinitions/>  
}
