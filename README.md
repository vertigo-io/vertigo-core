#Vertigo

A Simple Java Starter  kit for **real projects**.

Its main purpose is to publish simple and homogeneous APIs over more complex libraries.


#Modules
__Vertigo__ is splitted into modules.

## vertigo-core
### Build and configure your own modules 

* a fast, simple and lightweight Dependency Injection
* __app__ : everything to configure your app
* __component__ : everything about components (fast, simple and lightweight Dependency Injection, simple AOP, proxies)
* __locale__ : manage internationalization of your app (MessageTexts)
* __param__ : a simple way for your business and technical configuration (external and internal)
* __defintions__ : a way to store and access all definitions of your app
* __resource__ : a simple access to resource (builtin : lookup into classpath, webapp, filesystem with relative or absolute path)
* __lang__ : a set of usefull elements such as assertion
 

## vertigo-commons
### A set of common tools 

* __analytics__ : track your process calls, time & errors
* __cache__ : keep your objects in memory to improve performance
* __codec__ : transform object into another. (main builtin : HTML, SHA1, Base64, Compress, Serialize) 
* __daemon__ : manage all your application's daemons (registering, stats) 
* __eventbus__ : a simple event bus for handling events in your app  
* __node__ : a simple node management for cluster applications (topology, health, config)
* __config__ : read configs for your application (overridable, externalizable, aggregate multiple configs)
* __peg__ : a simple parser for your [DSL](http://en.wikipedia.org/wiki/Domain-specific_language)
* __script__ : execute String like script (because sometimes you need to merge code and data)
* __transaction__ : simple transaction managment 

## vertigo-database
### A simple data access to your databases

* __sql__ : with builtin handlers : Oracle, MSSql, Postgresql, Hsql, H2, Hibernate

## vertigo-dynamo
### A simple way to define your application's model and provides useful api over it like storage, search, etc...
  
* __collections__ : collections tools (builtin : fulltext indexation, facetting, filtering)   
* __criteria__ : a unique api to to build filters (predicates, sql)
* __domain__ : top-2-bottom POJO to simplify layers communications from Database to GUI/WS
* __environment__ : initialize your components from differents sources (builtin : powerdesigner, DSL, Java annotations)
* __file__ : manage file's creation
* __kvstore__ : key/value datastore
* __store__ : simple persistence layer access (builtin : route by object type, CRUD operations, NN operations)
* __search__ : simple search api
* __task__ : manage your tasks


## vertigo-account
### A simple managment of users, not only technical.

* __authentication__ : provide a set of connectors to easily manages your end users authentication in your app
* __authorization__ : userSession and security tools to check resources access (by user roles and/or datas properties)   
* __identity__ : a way to store and identify your users accounts


## vertigo-vega
### Push your apps to others.

* __rest__ : Add a rest access to your application. Mainly oriented for production-ready Single-Page-Application. And production's security ready.


## vertigo-studio
### A set of tools to help you through developpement
* __mda__ : Model Driven Architecture with Tools to generate sources, sql, js, ts, multilingual properties...
* __reporting__ : a set of tools to build indicators about your app
* __ui__ : more to come

## vertigo-bundle
A bundle of all these modules


## vertigo-parent
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
