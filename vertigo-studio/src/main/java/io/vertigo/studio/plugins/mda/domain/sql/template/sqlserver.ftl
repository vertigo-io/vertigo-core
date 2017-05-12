-- ============================================================
--   Nom de SGBD      :  ${basecible}                     
--   Date de création :  ${.now?date}  ${.now?time}                     
-- ============================================================

<#if drop>
-- ============================================================
--   Drop                                       
-- ============================================================
<#list nnAssociations as associationDefinition>

drop table ${associationDefinition.getTableName()};

</#list>
<#list dtDefinitions as dtDefinition>

drop table ${dtDefinition.localName};
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


<#list dtDefinitions as dtDefinition>
-- ============================================================
--   Table : ${dtDefinition.localName}                                        
-- ============================================================
create table ${dtDefinition.localName}
(
	<#list dtDefinition.fields as field>
	<#if field.persistent>
	<#-- For primary key we use the domain datatype to add 'identity'  -->
    ${field.name?right_pad(12)}${"\t"} ${sql(field)?right_pad(12)}${"\t"}<#if (field.id) && ("String" != field.javaType) >identity<#elseif field.required>not null<#else></#if>,
    </#if><#-- field.persistent -->
    </#list><#-- fieldCollection -->
    <#list dtDefinition.fields as field>
    <#if field.persistent>
    <#if field.id >
    constraint PK_${dtDefinition.localName} primary key nonclustered (${field.name})<#if tableSpaceIndex?has_content> USING INDEX TABLESPACE :TABLESPACE_NAME_INDEX</#if>
    </#if><#-- field.type -->
    </#if><#-- field.persistent -->
    </#list>
)<#if tableSpaceData?has_content>
TABLESPACE :TABLESPACE_NAME_DATA</#if>;

<#list dtDefinition.fields as field>
<#if field.persistent>
<#if field.display?has_content>
comment on column ${dtDefinition.localName}.${field.name} is
'${field.display?replace("'","''")}';

</#if>
</#if>
</#list>
</#list>

<#list simpleAssociations as associationDefinition>
<#if associationDefinition.getAssociationNodeA().getDtDefinition().isPersistent() && associationDefinition.getAssociationNodeB().getDtDefinition().isPersistent()>
<#assign associationLocalName = associationDefinition.getName()?substring(2)> 
alter table ${associationDefinition.getForeignAssociationNode().getDtDefinition().localName}
	add constraint FK_${associationLocalName}_${associationDefinition.getPrimaryAssociationNode().getDtDefinition().localName} foreign key (${associationDefinition.getFKField().getName()})
	references ${associationDefinition.getPrimaryAssociationNode().getDtDefinition().localName} (${associationDefinition.getPrimaryAssociationNode().getDtDefinition().getIdField().get().getName()});

create index ${associationLocalName}_${associationDefinition.getPrimaryAssociationNode().getDtDefinition().localName}_FK on ${associationDefinition.getForeignAssociationNode().getDtDefinition().localName} (${associationDefinition.getFKField().getName()} asc)<#if tableSpaceIndex?has_content>
TABLESPACE :TABLESPACE_NAME_INDEX</#if>;

</#if>
</#list>

<#list nnAssociations as associationDefinition>
<#if associationDefinition.getAssociationNodeA().getDtDefinition().isPersistent() && associationDefinition.getAssociationNodeB().getDtDefinition().isPersistent()>
<#assign associationLocalName = associationDefinition.getName()?substring(4)> 
create table ${associationDefinition.getTableName()}
(
	${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().getName()?right_pad(12)}${"\t"} ${sql(associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().domain)?right_pad(12)}${"\t"} not null,
	${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().getName()?right_pad(12)}${"\t"} ${sql(associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().domain)?right_pad(12)}${"\t"} not null,
	constraint PK_${associationDefinition.getTableName()} primary key (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().getName()}, ${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().getName()})<#if tableSpaceIndex?has_content> USING INDEX TABLESPACE :TABLESPACE_NAME_INDEX</#if>,
	constraint FK_${associationLocalName}_${associationDefinition.getAssociationNodeA().getDtDefinition().localName} 
		foreign key(${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().getName()})
		references ${associationDefinition.getAssociationNodeA().getDtDefinition().localName} (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().getName()}),
	constraint FK_${associationLocalName}_${associationDefinition.getAssociationNodeB().getDtDefinition().localName} 
		foreign key(${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().getName()})
		references ${associationDefinition.getAssociationNodeB().getDtDefinition().localName} (${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().getName()})
)<#if tableSpaceData?has_content>
TABLESPACE :TABLESPACE_NAME_DATA</#if>;

create index ${associationLocalName}_${associationDefinition.getAssociationNodeA().getDtDefinition().localName}_FK on ${associationDefinition.getTableName()} (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().getName()} asc)<#if tableSpaceIndex?has_content>
TABLESPACE :TABLESPACE_NAME_INDEX</#if>;

create index ${associationLocalName}_${associationDefinition.getAssociationNodeB().getDtDefinition().localName}_FK on ${associationDefinition.getTableName()} (${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().getName()} asc)<#if tableSpaceIndex?has_content>
TABLESPACE :TABLESPACE_NAME_INDEX</#if>;

</#if>
</#list>