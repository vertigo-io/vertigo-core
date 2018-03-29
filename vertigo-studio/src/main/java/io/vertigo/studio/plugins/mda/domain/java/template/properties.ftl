################################################################################");
# Attention ce fichier est généré automatiquement !");
# Resources du module ${packageName};
################################################################################");

<#list dtDefinitions as dtDefinition>
<#list dtDefinition.allFields as dtField>
${dtField.resourceKey} = ${dtField.label}
</#list>

################################################################################");

</#list>