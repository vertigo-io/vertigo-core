/**
 * These metadatas are generated automatically.
 * @type {Object}
 */
module.exports = {
<#list dtDefinitions as dtDefinition>
	"${dtDefinition.classSimpleNameCamelCase}": {
		<#list dtDefinition.dtFields as dtField>
			"${dtField.nameCamelCase}": {
				"domain": "${dtField.dtField.domain.name}",
				"required": ${dtField.notNull?string('true','false')}
			}<#if (dtField_index+1) < dtDefinition.dtFields?size>,</#if>		
		</#list>	
	}<#if (dtDefinition_index+1) < dtDefinitions?size>,</#if>
</#list>	
};
