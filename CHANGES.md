Version history
===============

Running 0.4.4-SNAPSHOT
----------------------
 more to come :)

Release 0.4.3 - 2014/08/06
--------------------------
* Fix Vega #5 error mimeType and Json syntaxe
* Fix Studio annotationNN package
* Fix Vega autoPagination support
* Fix tempo dependencies

Release 0.4.2 - 2014/08/01
--------------------------
* Refact vertigo-bundle : Add tempo and remove studio and labs
* Add metadatas support in DtList
* Add JsDtDefinitionGenerato in vertigo-studio for **Focus** (HTML5-SPA) applications
* Add file encoding parameter of generated files in vertigo-studio
* Fix file encoding template of loaders in vertigo-studio
* Add vertigo-tempo : Manage background operations and communications
* Merge collections.facet to collections
* Move back Transactional annotations from stereotype package
* Rename XxxStorePlugins as XxxDataStorePlugins
* Some refactoring by using List<Plugin> contructor injection
* Some minor internal renames


Release 0.4.1 - 2014/07/29
--------------------------
* Add Multi KvDataStore support
* Add List injection support in constructor
* Change DateQueryParser use elacticSearch syntax instead of Solr ones
* Move almost all annotations to a stereotype package
* Fix missing PathPrefix bug in Vega/rest component
* Fix nullValue in facetByTerm


Release 0.4.0 - 2014/07/25
--------------------------
The most notable changes in Vertigo 0.4.0 over previous releases are:  
  * Change ksp syntax to have a more similar json syntax.  Separator ';' is replaced by ','. Array {} is replaced by [] and  object notation () is replaced by {}
  * Change declaration of resources is externalized. In this way, each module can declare its own resources (oom, ea, ksp, xml).     
  * Change KDataType to DataType
  * Change : Tasks must be processed using TaskManager and not WorkManager. (In fact each component must be able to process sync or async its own works) 
  * Add module labs, contains some usefull components such as mail   
  * Add module persona, built to manage all concepts around a user/persona  
  * Add module quarto, which contains two components : publisher (to create new publications by merging template and data) and converter (to convert a doc, docx into a pdf for example)   
  * Add distributed task management (by redis or http/rest)
  * Add Enterprise Architect support
  * Add vertigo-vega : RESTful WebService support for HTML5 applications
  * Fix Issues #3 : add a new mandatory vertigo-studio parameter ```domain.dictionaryClassName``` 
    * To maintain compatibility add : ```domain.dictionaryClassName=DtDefinitions``` in your studio-config.properties
  * Add check on unused components, params during injection  
  * Fix Issues #2

Release 0.3.0 - 2014/03/13
--------------------------
The most notable changes in Vertigo 0.3.0 over previous releases are:
  * Add vertigo-studio
  * Add Jpa/Hibernate support
  * Add vertigo-dynamo
  * Add vertigo-ccc Command & Control Center
  * Refactor maven modules

Release 0.2.1 - 2014/02/26
--------------------------
  * Simplify Assertion api

Release 0.2.0 - 2014/02/26
--------------------------
The most notable changes in Vertigo 0.2.0 over previous releases are:
  * Refactor package and naming 
  * Add vertigo-commons tools, api and impl split
  * Add autocloseable

Release 0.1.0 - 2014/01/14
--------------------------
  * First release


