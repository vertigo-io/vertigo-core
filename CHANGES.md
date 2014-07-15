Version history
===============

Running 0.3.1-SNAPSHOT
----------------------
  * Change ksp syntax to have a more similar json syntax.  Separator ';' is replaced by ','. Array {} is replaced by [] and  object notation () is replaced by {}
  * Change declaration of resources is externalized. In this way, each module can declare its own resources (oom, ea, ksp, xml).     
  * Change KDataType to DataType
  * Change : Tasks must be processed using TaskManager and not WorkManager. (In fact each component must be able to process sync or async its own works) 
  * Add module labs, contains some usefull components such as mail   
  * Add module persona, built to manage all concepts around a user/persona  
  * Add module quarto, which contains two components : publisher (to create new publications by merging template and data) and converter (to convert a doc, docx into a pdf for example)   
  * Add distributed task management (by redis or http/rest)
  * Add Enterprise Architect support
  * Add vertigo-rest : RESTful WebService support for HTML5 applications
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


