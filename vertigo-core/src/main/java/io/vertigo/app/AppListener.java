/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.app;

/**
 * @author pchretien
 */
public interface AppListener {
	//Phases
	//Start App
	//0. start Boot
	//1.a read parameters
	//1.b read definitions
	//1.c read components  (create and start all components)

	//2.a start Boot >> start engines ???? 
	//2.a start parameterSpace 
	//2.b start paramSpace
	//2.c start definitionSpace
	//2.d start componentSpace : postInit components (Initializer)

	//Stop App
	//stop componentSpace : stop all components (reverse order)
	//stop definitionSpace : clear definitions 
	//stop paramSpace : clear paramss
	//Stop Boot >> Stop engines

	//	void onPostBoot();

	void onPostStart();
}
