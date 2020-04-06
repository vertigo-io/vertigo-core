/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.node.component;

/**
 * A core-component is one of tbe key concept of vertigo.
 * 
 * A core-component 
 * - is thread safe
 * - has parameters 
 * - a state
 * - offers services (technical or business)
 * 
 * There is 4 types of core-component
 *  - Component
 *  - Plugin 
 *  - Connector 
 *  - Amplifier 
 *  
 * Each core-component 
 *  - can be Activeable
 *  - can have aspects
 *  - can have parameters
 *  
 *  
 *              +----------+----------+--------+------------------------
 *              |    API   |  IMPL    | Scope  | ...can be injected 
 *  +-----------+----------+----------+--------+------------------------
 *  | Component | option   | required | app    | in all core-components
 *  +-----------+----------+----------+--------+------------------------
 *  | Amplifier | required | no       | app    | in all core-components
 *  +-----------+----------+----------+---------------------------------
 *  | Plugin    | required | no       | module | only in components
 *  +-----------+----------+----------+--------+------------------------
 *  | Connector | no       | required | app    | in all core-components 
 *  +-----------+----------+----------+---------------------------------
 *  
 *  Each core-component must be defined in a specific module.
 *  A plugin is a kind of private component (with a module scope),  
 *  it's used to abstract the way to bridge a specific java library.
 *  So, plugins concentrate all the dependencies. 
 *  This facilitates the evolutions when a version of a library is updated. 
 *  
 *  notes : 
 *  A "manager" is a special marker used to identify technical components created in vertigo.
 *  Each module can define its own type of component. 
 *  For example "webServices" may be the root of all webServices..
 *  This approach is very usefull to define a semantic structure of all the components.
 *  
 * @author pchretien
 */
public interface CoreComponent {
	//
}
