package ${suite.packageName};

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Cette classe est générée automatiquement par TaskTestGeneratorPlugin.  
*/
@RunWith(Suite.class)
@Suite.SuiteClasses({
	<#list suite.testClasses as testClass>
	${testClass.classCanonicalName}.class<#if testClass_has_next>, </#if>
	</#list>
})
public class AllTestsAo {
	// RAS.
}