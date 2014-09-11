package io.vertigo.struts2.context;

import io.vertigo.core.component.Manager;
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
