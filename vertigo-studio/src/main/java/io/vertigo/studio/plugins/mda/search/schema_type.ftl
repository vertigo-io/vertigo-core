		<!-- Projet / utilisateur (modifiables) -->
		<!-- non analyse -->
        <fieldType name="date" class="solr.TrieDateField"  sortMissingLast="true"/>
        <fieldType name="long" class="solr.SortableLongField"  sortMissingLast="true"/>
        <fieldType name="integer" class="solr.SortableIntField"  sortMissingLast="true"/>
        <fieldType name="double" class="solr.SortableDoubleField"  sortMissingLast="true"/>
        <fieldType name="boolean" class="solr.BoolField"  sortMissingLast="true"/>
        <fieldType name="string" class="solr.StrField"  sortMissingLast="true"/>
        <!-- analyse -->
        <fieldtype name="text" class="solr.TextField" positionIncrementGap="100"  sortMissingLast="true">
        	<analyzer>
        		<tokenizer class="solr.StandardTokenizerFactory" />
        		<filter class="solr.ASCIIFoldingFilterFactory" />
        		<filter class="solr.LowerCaseFilterFactory" />
        		<filter class="solr.ElisionFilterFactory" ignoreCase="true" articles="lang/contractions_fr.txt" /> <!-- supprimer l' m' n' ... -->
        		<filter class="solr.StopFilterFactory" ignoreCase="true" words="lang/stopwords_fr.txt" enablePositionIncrements="true" />        		
        		<filter class="solr.WordDelimiterFilterFactory" splitOnNumerics="0" splitOnCaseChange="1" catenateWords="1" preserveOriginal="1" />
        	</analyzer>
        </fieldtype>
        <fieldtype name="text.snowball" class="solr.TextField" positionIncrementGap="100"  sortMissingLast="true">
        	<analyzer>
        		<tokenizer class="solr.StandardTokenizerFactory" />
        		<filter class="solr.ASCIIFoldingFilterFactory" />
        		<filter class="solr.LowerCaseFilterFactory" />
        		<filter class="solr.ElisionFilterFactory" ignoreCase="true" articles="lang/contractions_fr.txt" /> <!-- supprimer l' m' n' ... -->
        		<filter class="solr.StopFilterFactory" ignoreCase="true" words="lang/stopwords_fr.txt" enablePositionIncrements="true" />        		
        	  <filter class="solr.WordDelimiterFilterFactory" splitOnNumerics="0" splitOnCaseChange="1" catenateWords="1" preserveOriginal="1" />
        		<filter class="solr.SnowballPorterFilterFactory" />
        	</analyzer>
        </fieldtype>
       
        <!-- 
        Le snowball anglais fonctionne assez correctement pour toutes les langues indo europeennes. 
        <fieldtype name="text.snowball.fr" class="solr.TextField" positionIncrementGap="100">
        	<analyzer>
        		<tokenizer class="solr.StandardTokenizerFactory" />
        		<filter class="solr.WordDelimiterFilterFactory" splitOnNumerics="0" splitOnCaseChange="1" catenateWords="1" preserveOriginal="1" />
        		<filter class="solr.ASCIIFoldingFilterFactory" />
        		<filter class="solr.LowerCaseFilterFactory" />
        		<filter class="solr.ElisionFilterFactory" ignoreCase="true" articles="elision.txt" /> <!- supprimer l' m' n' ... ->
        		<filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" enablePositionIncrements="true" />
        		<filter class="solr.SnowballPorterFilterFactory" language="french" />
        	</analyzer>
        </fieldtype>
        -->
        <fieldtype name="text.fr" class="solr.TextField" positionIncrementGap="100" sortMissingLast="true">
        	<analyzer>
        		<tokenizer class="solr.StandardTokenizerFactory" />
        		<filter class="solr.ASCIIFoldingFilterFactory" />
        		<filter class="solr.LowerCaseFilterFactory" />
        		<!-- En Francais on supprime les apostrophes -->
        		<filter class="solr.ElisionFilterFactory" ignoreCase="true" articles="lang/contractions_fr.txt" /> <!-- supprimer l' m' n' ... -->
        		<filter class="solr.StopFilterFactory" ignoreCase="true" words="lang/stopwords_fr.txt" enablePositionIncrements="true" />        		
        		<filter class="solr.WordDelimiterFilterFactory" splitOnNumerics="0" splitOnCaseChange="1" catenateWords="1" preserveOriginal="1" />
        		<filter class="solr.FrenchMinimalStemFilterFactory"/>
        		</analyzer>
        </fieldtype>
        <fieldtype name="text.en" class="solr.TextField" positionIncrementGap="100" sortMissingLast="true">
        	<analyzer>
        		<tokenizer class="solr.StandardTokenizerFactory" />
        		<filter class="solr.ASCIIFoldingFilterFactory" />
        		<filter class="solr.LowerCaseFilterFactory" />
        		<filter class="solr.StopFilterFactory" ignoreCase="true" words="lang/stopwords_en.txt" enablePositionIncrements="true" />        		
        	  <filter class="solr.EnglishPossessiveFilterFactory"/>
        		<!-- En Anglais on decoupe les mots suivant la casse -->
        		<filter class="solr.WordDelimiterFilterFactory" splitOnNumerics="0" splitOnCaseChange="1" catenateWords="1" preserveOriginal="1" />
        		<filter class="solr.EnglishMinimalStemFilterFactory"/>
        		</analyzer>
        </fieldtype>

