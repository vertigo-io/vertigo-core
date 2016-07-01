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
package io.vertigo.commons.impl.cache;

import java.io.Serializable;

import io.vertigo.commons.cache.CacheConfig;
import io.vertigo.lang.Plugin;

/**
 * Plugin de gestion de cache.
 *
 * @author pchretien
 */
public interface CachePlugin extends Plugin {
	/**
	 * Configuration des caches.
	 * Il est préférable d'appeler cette méthode une seule fois par type de cache et au démarrage.
	 * Il s'agit en effet d'une phase d'initialisation.
	 *
	 * @param context Contexte du cache
	 * @param cacheConfig Config of cache
	 */
	void addCache(final String context, final CacheConfig cacheConfig);

	/**
	 * Ajoute Objet dans le cache.
	 * Si le context n'existe pas, il est crée.
	 * Si la clé existe déjà, l'objet précédent est remplacé.
	 *
	 * @param context Contexte de cache
	 * @param key Clé de l'objet à insérer
	 * @param value Objet à insérer
	 */
	void put(final String context, final Serializable key, final Object value);

	/**
	 * Cette methode rend l'objet désigne par le contexte et le handle donnée en entrée.
	 * Si le contexte n'existe pas, une exception IllegalArgumentException.
	 * Si le handle n'existe pas, ou l'objet n'a pas ou plus de reference en cache, l'objet renvoyé et un null.
	 *
	 * @param context Contexte de cache
	 * @param key Clé de l'objet à récupérer
	 * @return Objet demandé ou null si non trouvé
	 */
	Object get(String context, Serializable key);

	/**
	 * Suppression du cache de l'objet référencé par sa clé.
	 *
	 * @param context Contexte de cache
	 * @param key Clé de l'objet à supprimer
	 * @return Si objet supprimé
	 */
	boolean remove(String context, Serializable key);

	/**
	 * Effacement du contenu d'un contexte.
	 * Supprime tous les items du cache.
	 *
	 * @param context Contexte de cache
	 */
	void clear(String context);

	/**
	 * Effacement du contenu de TOUS les Contextes de cache.
	 */
	void clearAll();
}
