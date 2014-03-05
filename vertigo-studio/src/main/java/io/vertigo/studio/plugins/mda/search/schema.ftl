<#import "schema_type.ftl" as type>
<?xml version="1.0" encoding="UTF-8" ?>
<schema name="${indexDefinition}" version="1.3">
	<types>
		<!-- Interne (non modifiables) -->
		<fieldtype name="internal.uri" class="solr.StrField" />
		<fieldType name="internal.binary" class="solr.BinaryField" />
		<fieldType name="internal.date" class="solr.TrieDateField" />
		<fieldtype name="internal.title" class="solr.TextField" positionIncrementGap="100">
			<analyzer>
				<tokenizer class="solr.StandardTokenizerFactory" />
				<filter class="solr.ASCIIFoldingFilterFactory" />
				<filter class="solr.LowerCaseFilterFactory" />
				<filter class="solr.ElisionFilterFactory" ignoreCase="true" articles="lang/contractions_fr.txt" /> <!-- supprimer l' m' n' ... -->
			</analyzer>
		</fieldtype>
		<!-- Projet -->
        <#include "schema_type.ftl">
	</types>

	<fields>
        <!-- Interne (non modifiables) -->
		<field name="URI" type="internal.uri" indexed="true" stored="true" required="true" />
        <!--<field name="TITLE" type="internal.title" indexed="true" stored="true" required="true" />-->
		<field name="FULL_RESULT" type="internal.binary" indexed="false" stored="true" required="true" />

        <!-- Projet -->
		<#list indexDefinition.indexDtDefinition.fields as dtField>
		<field name="${dtField.name}" type="${indexType(dtField.domain)}" indexed="true" stored="false" multiValued="false" />
		</#list>
	</fields>

	<uniqueKey>URI</uniqueKey>
	<defaultSearchField>CONTENT</defaultSearchField>
	<solrQueryParser defaultOperator="AND" />


</schema>
