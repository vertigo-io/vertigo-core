/**
 * Attention ce fichier est généré automatiquement !
 * ${simpleClassName}
 */

module.exports = {
    <#list dtDefinitions as dtDefinition>
    ${dtDefinition.classSimpleName?uncap_first}: {
        <#list dtDefinition.fields as dtField>
        ${dtField.camelCaseName}: "${dtField.label}"<#sep>,</#sep>
        </#list>
    }<#sep>,</#sep>
    </#list>
};
