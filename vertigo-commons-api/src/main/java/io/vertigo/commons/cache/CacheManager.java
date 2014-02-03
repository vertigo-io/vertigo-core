package io.vertigo.commons.cache;

import io.vertigo.kernel.component.Manager;

import java.io.Serializable;


/**
 * Manager des caches.
 *
 * Les caches sont g�r�s par contexte de cache (module, composant m�tier).
 * Les acc�s aux objets mis en cache s'effectuent par une cl�. (id/uri)
 *
 * @author pchretien
 * @version $Id: CacheManager.java,v 1.3 2013/10/22 10:42:32 pchretien Exp $
 */
public interface CacheManager extends Manager {
	/**
	 * Configuration des caches.
	 * Il est pr�f�rable d'appeler cette m�thode une seule fois par type de cache et au d�marrage.
	 * Il s'agit en effet d'une phase d'initialisation. 
	 * 
	 * @param cacheType Type du cache
	 * @param context Contexte du cache
	 * @param maxElementsInMemory Nombre maximal d'�l�ments mis en cache m�moire
	 * @param timeToLiveSeconds Dur�e maximale de conservation des donn�es en cache  
	 * @param timeToIdleSeconds Dur�e d'inactivit� au del� de laquelle le cache est vid�.
	 */
	void addCache(final String cacheType, final String context, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds);

	/**
	 * Ajoute Objet dans le cache.
	 * Si le context n'existe pas, il est cr�e.
	 * Si la cl� existe d�j�, l'objet pr�c�dent est remplac�.
	 *
	 * @param context Contexte de cache
	 * @param key Cl� de l'objet � ins�rer
	 * @param value Objet � ins�rer
	 */
	void put(final String context, final Serializable key, final Serializable value);

	/**
	 * Cette methode rend l'objet d�signe par le contexte et le handle donn�e en entr�e.
	 * Si le contexte n'existe pas, une exception IllegalArgumentException.
	 * Si le handle n'existe pas, ou l'objet n'a pas ou plus de reference en cache, l'objet renvoy� et un null.
	 *
	 * @param context Contexte de cache
	 * @param key Cl� de l'objet � r�cup�rer
	 * @return Objet demand� ou null si non trouv�
	 */
	Serializable get(String context, Serializable key);

	/**
	 * Suppression du cache de l'objet r�f�renc� par sa cl�.
	 *
	 * @param context Contexte de cache
	 * @param key Cl� de l'objet � supprimer
	 * @return Si objet supprim�
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
