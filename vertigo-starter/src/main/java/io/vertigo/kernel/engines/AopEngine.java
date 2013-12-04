/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.kernel.engines;

import io.vertigo.kernel.Engine;
import io.vertigo.kernel.aop.Interceptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


/**
 * Cr�ation des objets g�rant les r�f�rences sur les instances et impl�mentant les interceptions.(AOP)
 * 
 * @author pchretien
 */
public interface AopEngine extends Engine {

	/**
	 * Cr�e une r�f�rence sur l'instance du composant.
	 * 
	 * @param instance Instance source non proxifi�e
	 * @param joinPoints Points d'ex�cution 
	 * @return R�f�rence proxifi�e sur l'instance du composant
	 */
	Object create(final Object instance, Map<Method, List<Interceptor>> joinPoints);
}
