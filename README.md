# Vertigo

A Simple Java Starter starter kit for **real projects**.

Its main purpose is to publish simple and homogeneous APIs over more complex libraries.


#Modules
``Vertigo`` is splitted into modules.

## vertigo-core
Build and configure your own modules with 

* a set of usefull elements such as assertion, option
* a fast, simple and lightweight Dependency Injection
 



## vertigo-commons
A set of common tools 

* ``analytics`` : track your process calls, time & errors
* ``cache`` : keep your objects in memory to improve performance
* ``codec`` : transform object into another. (main builtin : HTML, SHA1, Base64, Compress, Serialize) 
* ``config`` : read configs for your application (overridable, externalizable, aggregate multiple configs)
* ``parser`` : a simple parser for your [DSL](http://en.wikipedia.org/wiki/Domain-specific_language)
* ``resource`` : a simple access to resource (builtin : lookup into classpath, webapp, filesystem with relative or absolute path)
* ``script`` : execute String like script (because sometimes you need to merge code and data)

## vertigo-dynamo
A simple data access to your sql/nosql database, including search patterns.
  
* ``collections`` : collections tools (builtin : fulltext indexation, facetting, filtering)   
* ``database`` : databases handlers (builtin : Oracle, MSSql, Postgresql, Hsql, H2, Hibernate)
* ``domain`` : top-2-bottom POJO to simplify layers communications from Database to GUI/WS
* ``environment`` : initialize your components from differents sources (builtin : powerdesigner, DSL, Java annotations)
* ``file`` : manage file's creation
* ``kvdatastore`` : key/value datastore
* ``persistence`` : simple persistence layer access (builtin : route by object type, CRUD operations, NN operations, SearchServer integration)
* ``search`` : simple search api
* ``task`` : manage your tasks
* ``transaction`` : simple transaction managment 
* ``work`` : process, shedule or distribute your task

## vertigo-rest 
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
