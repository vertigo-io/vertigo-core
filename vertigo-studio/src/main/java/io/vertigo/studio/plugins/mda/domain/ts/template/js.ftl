/*
    Ce fichier a été généré automatiquement.
    Toute modification sera perdue.
*/

/* tslint:disable */
<@compress single_line=true>
import {
<#if dtDefinition.isContainsPrimitiveField() || dtDefinition.isContainsObjectField()>
EntityField,
</#if>
<#if dtDefinition.isContainsListField()>
StoreListNode, EntityList,
</#if>
StoreNode }  from "focus4/entity";
</@compress>

<#if dtDefinition.isContainsPrimitiveField()>
import * as domains from "../../../common/domain"
</#if>
<#list dtDefinition.fields as field>
	<#if field.isPrimitive()>	
	<#else>
import { ${dtDefinition.classSimpleName}, ${dtDefinition.classSimpleName}Node } from "./${dtDefinition.localName?lower_case?replace("_", "-")}"
	</#if>
</#list>

export interface ${dtDefinition.classSimpleName} {
	<#list dtDefinition.fields as dtField>
	${dtField.camelCaseName}<#if !dtField.isRequired()>?</#if>: ${dtField.javascriptType};
    </#list>
}

export interface ${dtDefinition.classSimpleName}Node extends StoreNode<${dtDefinition.classSimpleName}> {
	<#list dtDefinition.fields as dtField>
	<#if dtField.isPrimitive()>
	${dtField.camelCaseName}: EntityField<${dtField.javascriptType}>;
	<#elseif dtField.isList()>
	${dtField.camelCaseName}: EntityList<StoreListNode<${dtField.dtField.domain.dtDefinition.classSimpleName}Node>>;
	<#else>
	${dtField.camelCaseName}: EntityField<${dtField.dtField.domain.dtDefinition.classSimpleName}Node>;
	</#if>
    </#list>
}

export const ${dtDefinition.classSimpleName}Entity = {
    name: "${dtDefinition.classSimpleName}",
    fields: {
    	<#list dtDefinition.fields as dtField>
        ${dtField.camelCaseName}: {
        	<#if dtField.isList()>
            type: "list" as "list",
            entityName: "${dtDefinition.classSimpleName?uncap_first}"        
        	<#else>
            name: "${dtField.camelCaseName}",
            type: "field" as "field",
            <#if dtField.isPrimitive()>
            domain: domains.${dtField.domainName},
            <#else>
            entityName: "${dtField.javascriptType?uncap_first}",
            </#if>
            isRequired: ${dtField.required?string("true","false")},
            translationKey: "${dtDefinition.functionnalPackageName}.${dtDefinition.classSimpleName}.${dtField.camelCaseName}"
          	</#if>            
        }<#if (dtField_index+1) < dtDefinition.fields?size>,</#if>
        </#list>
    }
};
