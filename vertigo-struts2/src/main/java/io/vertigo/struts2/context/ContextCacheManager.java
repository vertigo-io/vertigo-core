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
package io.vertigo.struts2.context;

import io.vertigo.lang.Manager;
import io.vertigo.struts2.core.KActionContext;

/**
 * Manager du cache des contexts.
 *
 * @author pchretien, npiedeloup
 */
public interface ContextCacheManager extends Manager {

	/**
	 * Ajoute Objet dans le cache.
	 * Si la clé existe déjà, l'objet précédent est remplacé.
	 *
	 * @param context Context à insérer
	 */
	void put(final KActionContext context);

	/**
	 * Cette methode rend l'objet désigne par le contexte et le handle donnée en entrée.
	 * Si la clé n'a pas ou plus de reference en cache, l'objet renvoyé et un null.
	 *
	 * @param key Clé de l'objet à récupérer
	 * @return Objet demandé ou null si non trouvé
	 */
	KActionContext get(String key);

}
