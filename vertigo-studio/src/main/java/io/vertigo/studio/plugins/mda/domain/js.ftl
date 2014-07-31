// ******************************************************************************** //
// **** SCRIPT GENERE, NE PAS MODIFIER MANUELLLEMENT ********** //
// ******************************************************************************** //

module.exports={
	<#list dtDefinitions as dtDefinition>
	'${dtDefinition.name}': {
		<#list dtDefinition.fields as dtField>
			'${dtField.nameLowerCase}':{
				domain: "${dtField.domain}",
				required: "${dtField.required}"
			},			
		</#list>	
	},
	</#list>	
};
