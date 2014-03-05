################################################################################");
# Attention ce fichier est généré automatiquement !");
# Resources du module ", packageName);
################################################################################");

<#list dtDefinitions as dtDefinition>
<#list dtDefinition.fields as dtField>
${dtField.resourceKey} = ${dtField.label.display}
</#list>

################################################################################");

</#list>