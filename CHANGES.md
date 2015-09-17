Version history
===============

Running 0.8.3-SNAPSHOT
----------------------

more to come :)


Release 0.8.2 - 2015/09/10
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-081-to-082)
__In Bold__ : Potential compatibility problems 
* [All] Lots of code cleaning and refactoring
* [All] Changed private methods that can be static to static
* [All] Refactored enties and dsl
* [All] Refactor java.lang.Timer to DeamonManager
* [All] Added Features to make AppConfig simpler
* [Core] Added putNullable on MapBuilder
* [Core] Param was nullable in builder and checked when injected
* [Core] Removed injection JSR250 support. Use Activeable interface exclusively
* [Core] Added check id type of URI (no more String into numeric PK)
* [Core] Renamed Home into App in tests
* [Commons] Plugged Analytics on WebService, Task, Job and Search
* [Commons] Replaced LocalEventsPlugin by a simple processor
* [Commons] Added DaemonManager for managed daemon task
* __[Dynamo] Added AnalyticsManager when using SearchManager__
* __[Dynamo] Removed inout params (now, a param is 'in' xor 'out')__
* [Dynamo] Added support of multiple values criteria into DefaultListFilterBuilder
* [Dynamo] Assertion to avoid multi TaskEngine invocation
* [Dynamo] Merged jpa into HibernateConnectionProviderPlugin
* [Dynamo] changed FileStore's api to obtain the same api than dataStore
* [Dynamo] Kvdatastore Berkeley can manage Set as HashSet
* [Dynamo] Splitted SearchLoader SQL logic and chunk logic
* __[Dynamo] Renamed Broker to DataStore__
* [Dynamo] Added DtObjectUtil.createUri by class and id
* [Dynamo] Fixed search reindex count
* [Studio] Removed Constants to generate a pretty code
* [Studio] Task has 0 or 1 result	
* [Studio] Fixed if report file already exist
* [Quarto] Error msg clearer
* [Struts2] Fixed stacking previous state of layout in tags (allow div tree, use it carefully :))
* [Persona] Added custom attributes on UserSession to register data from addons
* [Tempo] Added more tests
* [Tempo] Added log for error
* __[Vega] Replaced session attribute key : vertigo.rest.Session to vertigo.webservice.Session__ 
* __[Vega] Replaced SparkApplication by simpler VegaSparkFilter__ 
* [Vega] Fixed swagger for some path
* __[Vega] Renamed ApplicationServletContextListener to AppServletContextListener__, and removed Abstract parent
* __[Vega] Renamed ServletListener to AppServletListener__ 
* [Vega] Renamed HomeServletStarter to AppServletStarter 
* __[Vega] Renamed all RestXXX into WebServiceXXX__
* __[Vega] Renamed all WebServices related objects with WebService prefix__
* [Vega] No more override httpStatusCode if already set by WebService impl
* [Vega] Refactored RoutesRegisterPlugin to WebServerPlugin
* [Vega] Refactored JsonConverterHandler, __extract ServerState behaviour in a new optional plugin__
* [Vega] Renamed DtObjectExtended to a more generic ExtendedObject
* [Vega] Added option support in WS params
* [Vega] Added support to HTTP NotModified 304 response 
* [Vega] Renamed DefaultJsonReader to RequestJsonReader
* [Vega] Added URI json serialization
* [Vega] Added PATCH verb


Release 0.8.1 - 2015/07/31
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-080-to-081)
* [All] Lots of code cleaning and refactoring
* [Core] Renamed io.vertigo.core.Home.App to io.vertigo.core.App
* [Core] Renamed io.vertigo.core.boot to io.vertigo.core.config
* [Core] Renamed io.vertigo.core.di to io.vertigo.core.component.di
* [Core] Refactor XMLAppConfigBuilder 
* [Core] Refactor modules order/priority in managers.xml. Loading order are more predictible now : 
 1. boot module
 2. definitions
 3. other components module per module (as before)
* [Core] Moved LocalManager and ResourceManager from commons to core.
* [Core] Moved EnvironmentManager from dynamo to core
* [Dynamo] Fixed search case/accent sensitive while using wildcard
* [Dynamo] Fixed search securityFilter
* [Dynamo] Add default value to DefaultListFilterBuilder
* [Dynamo] return Future<Long> on reindexAll to allow join on reindex
* [Dynamo] CSVExporter should kept empty value as empty
* [Dynamo] Renamed ESSearchServicePlugin to ESNodeSearchServicePlugin
* [Dynamo] Fixed store object without index
* [Studio] Fix JS templates style, to fit with ESLint

Release 0.8.0 - 2015/07/02
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-075-to-080)
* [All] Fixed some code style and some refactor to simpler code
* [All] Renamed prefix in Builder with ==> add
* [Core] split BootConfig from AppConfig
* [Core] Refactoring DI
* [Core] Added better error message for missing definition while solving
* [Commons] Added daemonManager
* [Dynamo] Removed deprecated methods
* [Dynamo] Fixed FsTransactionResource, Temp files not deleted in some case
* [Dynamo] Added possibility to specify a search data type for index (in addition to the analyzer)
Use indexType : "analyzerName:dataTypeName" in domain declaration in KSP
* [Dynamo] Search return hightlights on result's fields only
* [Dynamo] Fixed search clustering create full list
* [Dynamo] Fixed and test #15
* [Dynamo] Renamed stereotype Subject to KeyConcept
* [Dynamo] Refactored and removed SearchIndexFieldNameResolver
* [Dynamo] Added EventsManager required by StoreManager (old PersistenceManager) 
* [Dynamo] Added RedisConnector
* [Dynamo] Complete refactor of SearchManager : integration in broker, KSP declaration of index definitions
* [Dynamo] Added FacetQueryResultMerger
* [Dynamo] Added DefaultSearchLoader and DefaultListFilterBuilder
* [Dynamo] Added ESSearchPlugin with TransportClient
* [Dynamo] Updated elasticSearch to 1.4.5
* [Dynamo] Fixed try with ressource of autonomous transaction
* [Dynamo, Studio] Renamed PersistenceManager to StoreManager
* [Dynamo, Studio] Renamed Broker to DataStore
* [Dynamo] Added Search facetValue sorted by count
* [Studio] Added optionnal dictionaryClassName param to specify dictionaryClassName, default to DtDefinitions (Fix #3)
* [Tempo] Added timeout in DistributedWorkerPlugins for connections from client to server
* [Tempo] Split scheduler and executor
* [Tempo] Added minute to jobManager.scheduleEveryDayAtHour
* [Quarto] Fixed UnitTests
* [Studio] Continue refactor to use stereotype KeyConcept in studio
* [Studio] Continue refactor to Search in generated DAO
* [Struts2] Fixed an error when escaping caracters in JSON
* [Struts2] Select multiple : listCssClass
* [Vega] TokenManager optionnal in JsonConverterHandler
*	[Vega] Fixed content-type name : now use '=' instead of ':'
* [Vega] Updated Swagger-ui to 2.1.0
* [Vega] Added support of FacetedQueryResult in WebService
* [Vega] Started expand jsonConverterHandlerPlugin into simplier JsonConverter per type


Release 0.7.5a - 2015/03/23
----------------------
* Fix of two 0.7.5 issues 


Release 0.7.5 - 2015/03/20
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-074-to-075)
* [Core] Renamed KTransaction to VTransaction
* [Dynamo] Renamed KSecurityManager to VSecurityManager
* [Dynamo] Renamed KFile to VFile
* [Dynamo] Renamed KxFileInfo to VxFileInfo
* [Dynamo] Added DtFieldName to FilterCriteria API
* [Dynamo] Added DtListState for sorting, offset and limit query. Applied on search only
* [Dynamo] Some code style
* [Studio] Fixed Dto generated with DtListURIForXXAssociation
* [Struts2] Fixed FTL : css, checkbox
* [Quarto] XDocReportConverterPlugin : XDocReport based ODT to PDF converter plugin


Release 0.7.4 - 2015/03/12
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-073-to-074)
* [All] Fixed some code style
* [All] Removed some deprecated : PersistenceManager.getBrokerConfiguration, PersistenceManager.getMasterDataConfiguration, UiMessageStack.hasErrorOnField, UiRequestUtil.getHttpServletRequest, UiRequestUtil.getHttpSession
* [Core] Aspects Order preserved
* [Core] Renamed vertigoimpl.commons to vertigo.commons.impl
* [Commons, Dynamo] Fixed index cacheability. For cache not serializable value. 
* [Dynamo] Added index settings check at startup
* [Dynamo] Fixed highlights on full_result field
* [Dynamo] Added Stereotype of DtObject : Data, MasterData or Subject
* [Dynamo] Refactored broker, cacheStore, SearchManager (introducing searchQueryBuilder)
* [Dynamo] Fixed searchQuery serializable
* [Dynamo] Added search clusering by facet, and FacetQuery is now optionnal, SearchQuery serializable
* [Dynamo] Fixed WhereIn with lowercase column name
*	[Dynamo] Fixed user exception detection/extraction
* [Dynamo] Updated ElasticSearch configFile
* [Struts2] Fixed readonly multi select
* [Struts2] Added assert uiMdList used with MasterData DtDefinition
* [Struts2] Renamed HomeServlerStarter to HomeServletStarter
* [Struts2] Added assert fieldName in camelCase
* [Struts2] Refactored formatter (no more FMT_DEFAULT)
* [Struts2][Vega] Added check of boot.applicationConfiguration
* [Struts2] Changed ftl usage as struts2.3.20
* [Struts2] Updated fielderror to add a onclick on error label to focus the field
* [Struts2] Updated to struts 2.3.20
* [Vega] Added encoder FacetedQueryResult to Json
* [Vega] Refactored RouteHandler to RestHandlerPlugin : make more configurable request processor stack
* [Vega] Added check response is commited when managed in controller
* [Studio] Made computed field always transient
* [Studio] Made generator target dir defined by plugins. Now you could generate : javagen, sqlgen, jsgen
* [Studio] Fixed hibernate sequence
* [Tempo] Added dead node detection of rest distributed
* [Quarto] Fixed #14

Release 0.7.3 - 2015/02/02
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-072-to-073)
* [All] Refactored tests to made them more autonomous
* [All] Fixed some code style
* [All] Cleaned some dependencies
* [Dynamo] Split DtListProcessor to dispatch between java or index powered operations
* [Dynamo] Made Id fieldName free for FileStorePlugin
* [Dynamo] Fixed Facet filtering
* [Dynamo] Fixed #12 in ES connection settings
* [Dynamo] Fixed FsFileStorePlugin error when target directory already exists
* [Dynamo] Simpler WhereInPreProcessor using with regexp
* [Dynamo] Fixed sorting crash on null valued field
* [Dynamo] Split fileInfoURI and DtObjectURI
* [Dynamo] Renamed datastore plugin : ...persistence.oracle.OracleDataStorePlugin to ...persistence.datastore.oracle.OracleDataStorePlugin
* [Dynamo] Refactored DaoBroker, Broker and Datastore
* [Studio] Changed hibernate generationType SEQUENCE to AUTO
* [Studio] Fixed crebase.sql a semicolon ommited when no tablespace is defined
* [Quarto] Added timeout to converterTask
* [Quarto] Renamed plugins to be compliant with naming rules (MergerPluginDOCX to DOCXMergerPlugin, same for OpenOffice)
* [Commons] Renamed MapCachePlugin into MemoryCachePlugin
* [Struts2][Vega] Force to prefix servlet parameters with 'boot.' (for orphan detection)
* [Struts2] ValidationUserException accept DtFieldName
* [Struts2] Fixed fieldname in ftl for ajax component
* [Struts2] Removed ``<br>`` in radiomap, should use css instead
* [Struts2] Added getBidDecimal in UiObject for validators use
* [Vega] Refactored CorsAllowers : extended, renamed as Handler, made optional
* [Vega] Changed RateLimiting window from 15mn to 5min
* [Vega] Fixed paginator
* [Vega] Splitted fieldErrors and objectFieldErrors for easier managment client side
* [Core] Renamed XMLAppConfigBuilder to XMLModulesBuilder
* [Core] Refactored AppConfig, simpler external LogConfig
* [Core] Merged App and Home
* [Studio,Core] Refactored Env parameters
* [Core] Fixed aspect bug


Release 0.7.2 - 2014/12/16
----------------------
* [Dynamo] Forced Hibernate sequence handler to use DB sequence
* [core] Fixed aspect bug
* [Studio] Fixed js generator, sql generator, updates dectector
* [Studio] Added sql tablespaces (data and index) : use optional params on SqlGenerator tableSpaceData and tableSpaceIndex


Release 0.7.1 - 2014/12/09
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-070-to-071)
* [Studio] Refactored : DomainGeneratorPlugin splited into more Plugins usages dependents (look at SqlGeneratorPlugin and JSGeneratorPlugin)
* [Studio] Fixed sql.ftl
* [Vega] UiListState in query instead of body
* [Dynamo] Fixed #10 and #11
* [Studio] ElasticSearch type mapping
* [Vega] Refacted JsonConverter and added DtList support in body
* [Vega] Added defaultValidator on Xxx&lt;O extends DtObject&gt;
* [Vega] fixed "already commited response" error
* [Vega] Added upload file test
* [Core] Refactored aspects : global scoped, simplier declarations with only one tag &lt;aspect&gt; with an attribute class extends Aspect


Release 0.7.0 - 2014/11/25
----------------------
[Migration help](https://github.com/KleeGroup/vertigo/wiki/Vertigo-Migration-Guide#from-06x-to-070)
A big version :)
* [All] Lots of code style fixes, tests units coverage, cleaning and refactoring
* [Core] Managers.xml syntax changed : Renamed &lt;modules&gt; to &lt;config&gt; (tag module doesn't change)
* [Core] Renamed two packages : io.vertigo.core.util, io.vertigo.core.lang
* [Core] Moved some classes : VUserException, DefinitionSpace, Engine
* [Core] Renamed ComponentSpaceConfigBuilder to AppConfigBuilder
* [Core] Injector static instead of instance
* [Core] Renamed @PreFixed to @DefinitionPreFixed and Manager to Component
* [Commons] Added CacheConfig
* [Dynamo] Moved search public api from SearchServicePlugin to SearchManager
* [Dynamo] Added possibility to override FormatterDefault rendering
* [Dynamo] Added fluent style with ExportBuilder, PropertiesBuilder
* [Dynamo] DtField is a DtFieldName	
* [Dynamo] Refactored ElasticSearch plugins	
* [Dynamo] **Work** is migrated to tempo
* [Vega] Fixed swagger contextPath, nnerBody params, inline test POST services
* [Vega] Moved DtListDelta and UiListDelta
* [Vega] Updated export api
* [Vega] Renamed UiSecurityTokenManager to TokenManager
* [Vega] Added DtObjectExtended support
* [Vega] Added check to unused pathParams
* [Vega] Added AutoPaginator support
* [Vega] Added CorsAllowerFilter to component (must be in managers.xml). Param originCORSFilter can be set here
* [Studio] Fixed properties generator
* [Struts2] Fixed index dto with FK bind to MDL
* [Struts2] Fixed #9, added a unique uriList when list put in context, and no cache for list without uri
* [Struts2] Multiple select in simple_read
* [Struts2] Added autocompleter popinURL	
* [Struts2 Vega] Fixed file download Content-Disposition
* [Struts2] Changed layout table behaviour	: now can include div in order to declare a single cell

Release 0.6.2 - 2014/10/27
----------------------
* [Struts2] Fixed 0.6.1 broken xxx_read themes inheritance
* [Struts2] Some ftl's tags fixed and improvments (autocompleter popinURL, div in div, reMoved some \n)
* [Vega] Fixed swagger contextPath
* [Studio] Fixed resources.ftl
* Fixed some tests

Release 0.6.1 - 2014/10/17
----------------------
* [Vega] Fixed relative path support with SwaggerUI
* [Vega] Moved DtListDelta and UiListDelta
* [Dynamo] Added possibility to override FormatterDefault rendering
* [Struts2] Added VFreemarkerManager to better override of struts2 FLT (Added this to your struts.xml *&lt;constant name="struts.freemarker.manager.classname" value="io.vertigo.struts2.impl.views.freemarker.VFreemarkerManager" /&gt;*, configure it with *TemplatePath* param in *web.xml*) 
* [Struts2] Moved ftl files to /io/vertigo/struts2/ftl/template 
* [Struts2] Disabled required mark for checkbox
* [Struts2] Added Multiple select tag support
* [Struts2] Fixed index dto with FK bind to MDL
* [Struts2] Fixed autocompleter theme support
* [Studio] Fixed properties generator
* Some CodeStyle fixes

Release 0.6.0 - 2014/10/15
----------------------
* [Core] managers.xml xsd now checks params are declared before plugins
* [Vega] Added support to Swagger Api viewer (see http://swagger.io/)
* [Vega] Added HttpRequest and HttpResponse parameters support
* [Vega] Added Header param support
* [Vega] Fixed some bug with query parameters
* [Dynamo] Some refacts (first step). No major api modifications.
* [Dynamo] Some renamed (DatabaseManager => SqlDatabaseManager)
* [Dynamo] Swap search from Solr to ElasticSearch 
* [Dynamo] Added Batch support to DAOBroker
* [Struts2] Fixed struts2 checkboxlist.ftl 
* [Struts2] Added a ftl jar for overrided ui components ftl
* [Struts2] Added check page access in SecurityFilter 
* [Studio] Refact some generated code for **Focus** compatibility
* [Persona] Refact dependencies
* [Persona] Renamed securityLoaderplugin into securityResourceLoaderPlugin
* [Tempo] Added authentification support in JavaxSendMailPlugin
* [Commons] Fixed CurrentLocale can provide an unsupported Locale
* Some minor fixes

Release 0.5.2 - 2014/09/23
--------------------------
* Some pom updated for MavenCentral Release :)
* Fixed Vega fileUpload support (with Servlet 3.0 API)
* Fixed struts2 .ftl for readonly checkbox, select, radiomap
* Fixed UiList usage
* Renames somes const
* Fixed .properties charset (ISO-8859-1)

Release 0.5.1 - 2014/09/17
--------------------------
* Fixed #7 ConstraintBigDecimal 
* Added Vega DtListDelta support (compatibility with **Focus** project)

Release 0.5.0 - 2014/09/16
--------------------------
* Added Struts2 module
* Added Studio Sql generation 
* Added Vega fileUpload support
* Refact WorkManager Api(Future), Distributed
* Refact CollectionManager Api(DtListProcessor)
* Renamed io.vertigo.kernel to io.vertigo.core
* Fixed CacheManager clear conditions
* Fixed Vega #6 Changed mime type for json entity
* Fixed Vega multiPath params support
* Updated dependencies

Release 0.4.3 - 2014/08/06
--------------------------
* Fixed Vega #5 error mimeType and Json syntaxe
* Fixed Studio annotationNN package
* Fixed Vega autoPagination support
* Fixed tempo dependencies

Release 0.4.2 - 2014/08/01
--------------------------
* Refact vertigo-bundle : added tempo and reMoved studio and labs
* Added metadatas support in DtList
* Added JsDtDefinitionGenerato in vertigo-studio for **Focus** (HTML5-SPA) applications
* Added file encoding parameter of generated files in vertigo-studio
* Fixed file encoding template of loaders in vertigo-studio
* Added vertigo-tempo : Manage background operations and communications
* Merge collections.facet to collections
* Moved back Transactional annotations from stereotype package
* Renamed XxxStorePlugins as XxxDataStorePlugins
* Some refactoring by using List<Plugin> contructor injection
* Some minor internal renames


Release 0.4.1 - 2014/07/29
--------------------------
* Added Multi KvDataStore support
* Added List injection support in constructor
* Changed DateQueryParser use elacticSearch syntax instead of Solr ones
* Moved almost all annotations to a stereotype package
* Fixed missing PathPreFixed bug in Vega/rest component
* Fixed nullValue in facetByTerm


Release 0.4.0 - 2014/07/25
--------------------------
The most notable changes in Vertigo 0.4.0 over previous releases are:  
  * Changed ksp syntax to have a more similar json syntax.  Separator ';' is replaced by ','. Array {} is replaced by [] and  object notation () is replaced by {}
  * Changed declaration of resources is externalized. In this way, each module can declare its own resources (oom, ea, ksp, xml).     
  * Changed KDataType to DataType
  * Changed : Tasks must be processed using TaskManager and not WorkManager. (In fact each component must be able to process sync or async its own works) 
  * Added module labs, contains some usefull components such as mail   
  * Added module persona, built to manage all concepts around a user/persona  
  * Added module quarto, which contains two components : publisher (to create new publications by merging template and data) and converter (to convert a doc, docx into a pdf for example)   
  * Added distributed task management (by redis or http/rest)
  * Added Enterprise Architect support
  * Added vertigo-vega : RESTful WebService support for HTML5 applications
  * Fixed Issues #3 : added a new mandatory vertigo-studio parameter ```domain.dictionaryClassName``` 
  * To maintain compatibility Added : ```domain.dictionaryClassName=DtDefinitions``` in your studio-config.properties
  * Added check on unused components, params during injection  
  * Fixed Issues #2

Release 0.3.0 - 2014/03/13
--------------------------
The most notable changes in Vertigo 0.3.0 over previous releases are:
  * Added vertigo-studio
  * Added Jpa/Hibernate support
  * Added vertigo-dynamo
  * Added vertigo-ccc Command & Control Center
  * Refactor maven modules

Release 0.2.1 - 2014/02/26
--------------------------
  * Simplify Assertion api

Release 0.2.0 - 2014/02/26
--------------------------
The most notable changes in Vertigo 0.2.0 over previous releases are:
  * Refactor package and naming 
  * Added vertigo-commons tools, api and impl split
  * Added autocloseable

Release 0.1.0 - 2014/01/14
--------------------------
  * First release


