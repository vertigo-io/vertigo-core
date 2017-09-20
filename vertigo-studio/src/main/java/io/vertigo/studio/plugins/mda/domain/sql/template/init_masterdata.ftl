<#list masterdatas as masterdata>
-- ============================================================
--   Insert MasterData values : ${masterdata.tableName}                                        
-- ============================================================
	<#list masterdata.values as value>
insert into ${masterdata.tableName}(<#list masterdata.definition.fields as field>${field.name}<#sep>, </#sep></#list>) values (<#list masterdata.definition.fields as field>'${value.getFieldValue(field.name)}'<#sep>, </#sep></#list>);
	</#list>
</#list>