-- ============================================================
--   Nom de SGBD      :  ${basecible}                     
--   Date de création :  ${.now?date}  ${.now?time}                     
-- ============================================================

<#if drop>
-- ============================================================
--   Drop                                       
-- ============================================================
<#list associations as associationDefinition>
<#if !associationDefinition.isAssociationSimpleDefinition()><#-- On drop les tables correspondant aux NN -->

drop table ${associationDefinition.getTableName()} cascade;
</#if>
</#list>
<#list dtDefinitions as dtDefinition>

drop table ${dtDefinition.dtDefinition.localName} cascade;
</#list>

</#if>

<#if tableSpaceData?has_content>
-- Tablespace des données.
\set TABLESPACE_NAME_DATA '${tableSpaceData}'
</#if>
<#if tableSpaceIndex?has_content>
-- Tablespace des indexes.
\set TABLESPACE_NAME_INDEX '${tableSpaceIndex}'
</#if>

-- ============================================================
--   Sequences                                      
-- ============================================================
<#list dtDefinitions as dtDefinition>
<#if dtDefinition.dtDefinition.persistent>
create sequence SEQ_${dtDefinition.dtDefinition.localName}
	start with 1000 cache 20; 

</#if>
</#list>

<#list dtDefinitions as dtDefinition>
<#if dtDefinition.dtDefinition.persistent>
-- ============================================================
--   Table : ${dtDefinition.dtDefinition.localName}                                        
-- ============================================================
create table ${dtDefinition.dtDefinition.localName}
(
	<#list dtDefinition.dtFields as field>
	<#if field.dtField.persistent>
    ${field.name?right_pad(12)}${"\t"} ${sql(field.dtField.domain)?right_pad(12)}${"\t"}<#if field.notNull>not null</#if>,
    </#if><#-- field.persistent -->
    </#list><#-- fieldCollection -->
    <#list dtDefinition.dtFields as field>
    <#if field.dtField.persistent>
    <#if "PRIMARY_KEY" == field.dtField.type >
    constraint PK_${dtDefinition.dtDefinition.localName} primary key (${field.name})<#if tableSpaceIndex?has_content> USING INDEX TABLESPACE :TABLESPACE_NAME_INDEX</#if>
    </#if><#-- field.type -->
    </#if><#-- field.persistent -->
    </#list>
)<#if tableSpaceData?has_content>
TABLESPACE :TABLESPACE_NAME_DATA</#if>;

<#list dtDefinition.dtFields as field>
<#if field.dtField.persistent>
<#if field.display?has_content>
comment on column ${dtDefinition.dtDefinition.localName}.${field.name} is
'${field.display?replace("'","''")}';

</#if>
<#if "FOREIGN_KEY" == field.dtField.type >
create index <#if (truncateNames && dtDefinition.dtDefinition.localName?length >5)>${dtDefinition.dtDefinition.localName?substring(0,5)}<#else>${dtDefinition.dtDefinition.localName}</#if>_${field.name}_FK on ${dtDefinition.dtDefinition.localName} (${field.name} asc)<#if tableSpaceIndex?has_content>
TABLESPACE :TABLESPACE_NAME_INDEX</#if>;
</#if>
</#if>
</#list>
</#if>
</#list>

<#list associations as associationDefinition>
<#if associationDefinition.getAssociationNodeA().getDtDefinition().isPersistent() && associationDefinition.getAssociationNodeB().getDtDefinition().isPersistent()>
<#if associationDefinition.isAssociationSimpleDefinition()>
alter table ${associationDefinition.getForeignAssociationNode().getDtDefinition().localName}
	add constraint FK_${associationDefinition.getName()?substring(2)} foreign key (${associationDefinition.getFKField().name()})
	references ${associationDefinition.getPrimaryAssociationNode().getDtDefinition().localName} (${associationDefinition.getPrimaryAssociationNode().getDtDefinition().getIdField().get().name()});
<#else>
create table ${associationDefinition.getTableName()}
(
	${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name()?right_pad(12)}${"\t"} ${sql(associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().domain)?right_pad(12)}${"\t"} not null,
	${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name()?right_pad(12)}${"\t"} ${sql(associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().domain)?right_pad(12)}${"\t"} not null,
	constraint PK_${associationDefinition.getTableName()} primary key (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name()}, ${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name()})<#if tableSpaceIndex?has_content> USING INDEX TABLESPACE :TABLESPACE_NAME_INDEX</#if>,
	constraint FK_${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeA().getDtDefinition().localName} 
		foreign key(${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name()})
		references ${associationDefinition.getAssociationNodeA().getDtDefinition().localName} (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name()}),
	constraint FK_${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeB().getDtDefinition().localName} 
		foreign key(${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name()})
		references ${associationDefinition.getAssociationNodeB().getDtDefinition().localName} (${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name()})
)<#if tableSpaceData?has_content>
TABLESPACE :TABLESPACE_NAME_DATA</#if>;

create index ${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeA().getDtDefinition().localName}_FK on ${associationDefinition.getTableName()} (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name()} asc)<#if tableSpaceIndex?has_content>
TABLESPACE :TABLESPACE_NAME_INDEX</#if>;

create index ${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeB().getDtDefinition().localName}_FK on ${associationDefinition.getTableName()} (${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name()} asc)<#if tableSpaceIndex?has_content>
TABLESPACE :TABLESPACE_NAME_INDEX</#if>;
</#if>

</#if>
</#list>
