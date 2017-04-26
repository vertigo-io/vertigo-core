/**
 * Attention ce fichier est généré automatiquement !
 * ${simpleClassName}
 */

module.exports = {
    <#list dtDefinitions as dtDefinition>
    ${dtDefinition.classSimpleNameCamelCase}: {
        <#list dtDefinition.dtFields as dtField>
        ${dtField.nameCamelCase}: "${dtField.display}"<#sep>,</#sep>
        </#list>
    }<#sep>,</#sep>
    </#list>
};
