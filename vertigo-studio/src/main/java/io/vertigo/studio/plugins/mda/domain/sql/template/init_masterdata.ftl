<#list masterdatas as masterdata>
-- ============================================================
--   Insert MasterData values : ${masterdata.tableName}                                        
-- ============================================================
	<#list masterdata.values as value>
insert into ${masterdata.tableName}(<#list masterdata.definition.fields as field><#if field.persistent>${field.name}<#sep>, </#sep></#if></#list>) values (<#list masterdata.definition.fields as field><#if field.persistent>'${value.getFieldValue(field.name)}'<#sep>, </#sep></#if></#list>);
	</#list>
</#list>