/*
    Ce fichier a été généré automatiquement.
    Toute modification sera perdue.
*/

/* tslint:disable */
<@compress single_line=true>
import {
<#if dtDefinition.isContainsPrimitiveField()>
EntityField,
</#if>
<#if dtDefinition.isContainsListOfObjectField()>
StoreListNode,
</#if>
StoreNode }  from "focus4/entity";
</@compress>

<#if dtDefinition.isContainsPrimitiveField()>
import * as domains from "../../../../00-core/domain"
</#if>
<#list dtDefinition.domains as domain>
	<#if domain.isPrimitive()>	
	<#else>
<#--import { ${dtDefinition.classSimpleName}, ${dtDefinition.classSimpleName}Node } from "./${dtDefinition.localName?lower_case?replace("_", "-")}" -->
import { ${domain.domainTypeName}, ${domain.domainTypeName}Node } from "./${domain.domainDefinitionName?lower_case?replace("_", "-")}"
	</#if>
</#list>

export interface ${dtDefinition.classSimpleName} {
	<#list dtDefinition.fields as dtField>
	${dtField.camelCaseName}<#if !dtField.isRequired()>?</#if>: ${dtField.typescriptType};
    </#list>
}

export interface ${dtDefinition.classSimpleName}Node extends StoreNode<${dtDefinition.classSimpleName}> {
	<#list dtDefinition.fields as dtField>
	<#if dtField.isPrimitive()>
	${dtField.camelCaseName}: EntityField<${dtField.typescriptType}, typeof ${dtField.domainName}>;
	<#elseif dtField.isListOfObject()>
	${dtField.camelCaseName}: StoreListNode<${dtField.domainTypeName}Node>;
	<#else>
	${dtField.camelCaseName}: EntityField<${dtField.domainTypeName}Node>;
	</#if>
    </#list>
}

export const ${dtDefinition.classSimpleName}Entity = {
    name: "${dtDefinition.classSimpleName?uncap_first}",
    fields: {
    	<#list dtDefinition.fields as dtField>
        ${dtField.camelCaseName}: {        
            name: "${dtField.camelCaseName}",
        	<#if dtField.isPrimitive()>
            type: "field" as "field",
			<#elseif dtField.isListOfObject()>
            type: "list" as "list",
           	<#else>
            type: "object" as "object",
            </#if>            
            <#if dtField.isPrimitive()>
            domain: domains.${dtField.domainName},
            <#else>
            entityName: "${dtField.domainTypeName?uncap_first}",
            </#if>
            isRequired: ${dtField.required?string("true","false")},
            translationKey: "${dtDefinition.functionnalPackageName}.${dtDefinition.classSimpleName?uncap_first}.${dtField.camelCaseName}"
        }<#sep>,</#sep>
        </#list>
    }
};
