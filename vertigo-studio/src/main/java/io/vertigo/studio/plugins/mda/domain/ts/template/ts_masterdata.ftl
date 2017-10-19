/*
    Ce fichier a été généré automatiquement.
    Toute modification sera perdue.
*/
<#list masterdatas as masterdata>
export type  ${masterdata.definition.classSimpleName}Code = <#list masterdata.values as value>"${value.getFieldValue(masterdata.idFieldName)}"<#sep> | </#sep></#list>;
export interface  ${masterdata.definition.classSimpleName} {
<#list masterdata.definition.fields as dtField>
	${dtField.camelCaseName}<#if !dtField.isRequired()>?</#if>: ${dtField.typescriptType};
</#list>
}
</#list>
