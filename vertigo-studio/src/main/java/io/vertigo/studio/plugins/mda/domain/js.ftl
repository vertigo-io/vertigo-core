/**
 * These metadatas are generated automatically.
 * @type {Object}
 */
module.exports={
	<#list dtDefinitions as dtDefinition>
	"${dtDefinition.classSimpleNameCamelCase}": {
		<#list dtDefinition.dtFields as dtField>
			"${dtField.nameCamelCase}":{
				domain: "${dtField.dtField.domain.name}",
				required: ${dtField.notNull?string('true','false')}
			},			
		</#list>	
	},
	</#list>	
};
