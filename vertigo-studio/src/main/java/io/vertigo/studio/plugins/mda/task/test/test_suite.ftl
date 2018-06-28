package ${suite.packageName};

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
<#list suite.testClasses as testClass>
import ${testClass.classCanonicalName};
</#list>

/**
 * Cette classe est générée automatiquement par TaskTestGeneratorPlugin.  
*/
@RunWith(Suite.class)
@Suite.SuiteClasses({
	<#list suite.testClasses as testClass>
	${testClass.className}.class<#if testClass_has_next>, </#if>
	</#list>
})
public class AllTests {
	// RAS.
}