package io.vertigo.commons.impl.cache;

import io.vertigo.kernel.component.Plugin;

import java.io.Serializable;

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
	 * @param cacheType Type du cache
	 * @param context Contexte du cache
	 * @param maxElementsInMemory Nombre maximal d'éléments mis en cache mémoire
	 * @param timeToLiveSeconds Durée maximale de conservation des données en cache  
	 * @param timeToIdleSeconds Durée d'inactivité au delé de laquelle le cache est vidé.
	 */
	void addCache(final String cacheType, final String context, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds);

	/**
	 * Ajoute Objet dans le cache.
	 * Si le context n'existe pas, il est crée.
	 * Si la clé existe déjà, l'objet précédent est remplacé.
	 *
	 * @param context Contexte de cache
	 * @param key Clé de l'objet à insérer
	 * @param value Objet à insérer
	 */
	void put(final String context, final Serializable key, final Serializable value);

	/**
	 * Cette methode rend l'objet désigne par le contexte et le handle donnée en entrée.
	 * Si le contexte n'existe pas, une exception IllegalArgumentException.
	 * Si le handle n'existe pas, ou l'objet n'a pas ou plus de reference en cache, l'objet renvoyé et un null.
	 *
	 * @param context Contexte de cache
	 * @param key Clé de l'objet à récupérer
	 * @return Objet demandé ou null si non trouvé
	 */
	Serializable get(String context, Serializable key);

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
