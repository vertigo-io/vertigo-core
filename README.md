#Vertigo

A Simple Java Starter  kit for **real projects**.

Its main purpose is to publish simple and homogeneous APIs over more complex libraries.


#Modules
__Vertigo__ is splitted into modules.

## vertigo-core
Build and configure your own modules with 

* a set of usefull elements such as assertion, option
* a fast, simple and lightweight Dependency Injection
 


##vertigo-commons
A set of common tools 

* __analytics__ : track your process calls, time & errors
* __cache__ : keep your objects in memory to improve performance
* __codec__ : transform object into another. (main builtin : HTML, SHA1, Base64, Compress, Serialize) 
* __config__ : read configs for your application (overridable, externalizable, aggregate multiple configs)
* __parser__ : a simple parser for your [DSL](http://en.wikipedia.org/wiki/Domain-specific_language)
* __resource__ : a simple access to resource (builtin : lookup into classpath, webapp, filesystem with relative or absolute path)
* __script__ : execute String like script (because sometimes you need to merge code and data)

##vertigo-dynamo
A simple data access to your sql/nosql database, including search patterns.
  
* __collections__ : collections tools (builtin : fulltext indexation, facetting, filtering)   
* __database__ : databases handlers (builtin : Oracle, MSSql, Postgresql, Hsql, H2, Hibernate)
* __domain__ : top-2-bottom POJO to simplify layers communications from Database to GUI/WS
* __environment__ : initialize your components from differents sources (builtin : powerdesigner, DSL, Java annotations)
* __file__ : manage file's creation
* __kvdatastore__ : key/value datastore
* __persistence__ : simple persistence layer access (builtin : route by object type, CRUD operations, NN operations, SearchServer integration)
* __search__ : simple search api
* __task__ : manage your tasks
* __transaction__ : simple transaction managment 
* __work__ : process, shedule or distribute your task

##vertigo-rest 
add a rest access to your application

##vertigo-ccc
Command & Control Center

Have a total control of your cluster by a json api

 * Config : list and stats about your modules 
 * System : check health of your system   
  

##vertigo-studio
Model Driven  Architecture

Tools to generate sources, sql, multilingual properties...


##vertigo-parent
just the parent pom

-----
#License
                Copyright (C) 2014, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
                KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
                
                Licensed under the Apache License, Version 2.0 (the "License");
                you may not use this file except in compliance with the License.
                You may obtain a copy of the License at
                
                http://www.apache.org/licenses/LICENSE-2.0
                
                Unless required by applicable law or agreed to in writing, software
                distributed under the License is distributed on an "AS IS" BASIS,
                WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                See the License for the specific language governing permissions and
                limitations under the License.
