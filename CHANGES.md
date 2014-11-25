Version history
===============

Running 0.7.1-SNAPSHOT
----------------------

more to come :)


Running 0.7.0 - 2014/11/25
----------------------
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
* [Dynamo] Refact ElasticSearch plugins	
* [Dynamo] Work are migrated to tempo
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


