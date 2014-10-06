/**
 * Attention ce fichier est généré automatiquement !
 * ${simpleClassName}
 */

module.exports =  {
  <#list dtDefinitions as dtDefinition>
    <#list dtDefinition.dtFields as dtField>
      "${dtField.nameCamelCase}" : "${dtField.display}",
    </#list>
  </#list>
};