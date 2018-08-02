package ${pao.taskDefinition.testPackageName};

import javax.inject.Inject;
import org.junit.Test;
import ${pao.daoTestBaseClass};

public class ${pao.taskDefinition.testClassName} extends ${pao.daoTestBaseClassSimpleName} {
	
	@Inject
	${pao.taskDefinition.className} ${pao.taskDefinition.daoVariable};

	@Test
	public void ${pao.taskDefinition.testMethodName}(){		
		this.check().semantics(() -> ${pao.taskDefinition.daoVariable}.${pao.taskDefinition.methodName}(<#list pao.taskDefinition.inAttributes as taskAttribute>${taskAttribute.value}<#if taskAttribute_has_next>, </#if></#list>));
	}
}