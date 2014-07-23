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
-- ============================================================
--   Sequences                                      
-- ============================================================
<#list dtDefinitions as dtDefinition>
create sequence SEQ_${dtDefinition.dtDefinition.localName}
	start with 1000 cache 20; 

</#list>

<#list dtDefinitions as dtDefinition>
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
    constraint PK_${dtDefinition.dtDefinition.localName} primary key (${field.name})
    </#if><#-- field.type -->
    </#if><#-- field.persistent -->
    </#list>
);

<#list dtDefinition.dtFields as field>
<#if field.dtField.persistent>
<#if field.display?has_content>
comment on column ${dtDefinition.dtDefinition.localName}.${field.name} is
'${field.display?replace("'","''")}';

</#if>
<#if "FOREIGN_KEY" == field.dtField.type >
create index <#if (dtDefinition.dtDefinition.localName?length >5)>${dtDefinition.dtDefinition.localName?substring(0,5)}<#else>${dtDefinition.dtDefinition.localName}</#if>_${field.name}_FK on ${dtDefinition.dtDefinition.localName} (${field.name} asc);

</#if>
</#if>
</#list>
</#list>

<#list associations as associationDefinition>
<#if associationDefinition.isAssociationSimpleDefinition()>
alter table ${associationDefinition.getForeignAssociationNode().getDtDefinition().localName}
	add constraint FK_${associationDefinition.getName()?substring(2)} foreign key (${associationDefinition.getFKField().name})
	references ${associationDefinition.getPrimaryAssociationNode().getDtDefinition().localName} (${associationDefinition.getPrimaryAssociationNode().getDtDefinition().getIdField().get().name});
<#else>
create table ${associationDefinition.getTableName()}
(
	${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name?right_pad(12)}${"\t"} ${sql(associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().domain)?right_pad(12)}${"\t"} not null,
	${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name?right_pad(12)}${"\t"} ${sql(associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().domain)?right_pad(12)}${"\t"} not null,
	constraint PK_${associationDefinition.getTableName()} primary key (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name}, ${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name}),
	constraint FK_${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeA().getDtDefinition().localName} 
		foreign key(${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name})
		references ${associationDefinition.getAssociationNodeA().getDtDefinition().localName} (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name}),
	constraint FK_${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeB().getDtDefinition().localName} 
		foreign key(${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name})
		references ${associationDefinition.getAssociationNodeB().getDtDefinition().localName} (${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name})
);

create index ${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeA().getDtDefinition().localName}_FK on ${associationDefinition.getTableName()} (${associationDefinition.getAssociationNodeA().getDtDefinition().getIdField().get().name} asc);

create index ${associationDefinition.getName()?substring(2)}_${associationDefinition.getAssociationNodeB().getDtDefinition().localName}_FK on ${associationDefinition.getTableName()} (${associationDefinition.getAssociationNodeB().getDtDefinition().getIdField().get().name} asc);
</#if>

</#list>

