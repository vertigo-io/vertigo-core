/**
 * Attention ce fichier est généré automatiquement !
 * ${simpleClassName}
 */

module.exports = {
    <#list dtDefinitions as dtDefinition>
    ${dtDefinition.classSimpleNameCamelCase}: {
        <#list dtDefinition.dtFields as dtField>
        ${dtField.nameCamelCase}: "${dtField.display}"<#if (dtField_index+1) < dtDefinition.dtFields?size>,</#if>
        </#list>
    }<#if (dtDefinition_index+1) < dtDefinitions?size>,</#if>
    </#list>
};
