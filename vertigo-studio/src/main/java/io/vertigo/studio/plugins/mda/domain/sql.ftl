-- ============================================================
--   Nom de la base   :  MODELE_5                              
--   Nom de SGBD      :  ORACLE Version 8                      
--   Date de création :  18/07/2002  10:46                     
-- ============================================================

<#list dtDefinitions as dtDefinition>
drop table ${dtDefinition.localName} cascade constraints
/
</#list>

<#list dtDefinitions as dtDefinition>
-- ============================================================
--   Table : ${dtDefinition.localName}                                        
-- ============================================================
create table ${dtDefinition.localName}
(
	<#list dtDefinition.fields as field>
	<#if field.persistent>
    ${field.name?right_pad(12)}${"\t"} ${sql(field.domain.dataType)?right_pad(12)}${"\t"}<#if field.notNull>not null</#if>,
    </#if><#-- field.persistent -->
    </#list><#-- fieldCollection -->
    <#list dtDefinition.fields as field>
    <#if field.persistent>
    <#if "PRIMARY_KEY" == field.type >
    constraint PK_PROFIL primary key (${field.name})
    </#if><#-- field.type -->
    </#if><#-- field.persistent -->
    </#list>
)
/
</#list>
<#list dtDefinitions as dtDefinition>
alter table ${dtDefinition.localName}
	<#list dtDefinition.fields as field>
	<#if field.FKDtDefinition?has_content>
    add constraint ##FK_UTIL_ASSOC_134_PROF## foreign key  (${field.name}) references ##PROFIL## (${field.name})
    </#if><#-- FKDtDefinition -->
    </#list>
/
</#list>

