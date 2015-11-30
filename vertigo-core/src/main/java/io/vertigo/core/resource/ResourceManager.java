package io.vertigo.core.resource;

import io.vertigo.lang.Manager;

import java.net.URL;

/**
 * Selecteurs de ressources.
 * Les ressources sont identifiées par une URL.
 * Cette URL peut être
 *  - relative au classpath de l'application dans le cas d'une application JAVA
 *  - relative au context de l'application WEB
 *
 * La ressource peut aussi être résolue de façon ad-hoc par la création d'un plugin de résolution spécifique.
 *
 * Les fichiers de configuration sont à considérer comme des ressources.
 * Ex:
 * 	classpath:
 * 		/myproject/components/components-config.dtd
 * 	web:
 *     /WEB-INF/components-config.xml
 *
 * L'implémentation permet de définir une liste de plusieurs plugins de résolutions de ressources.
 * Il est aussi possible d'enregistrer des @see ResourceResolverPlugin spécifique. (Par exemple pour stocker les ressources en BDD)
 *
 * @author pchretien
 */
public interface ResourceManager extends Manager {
	/**
	 * Retourne une URL à partir de sa représentation 'chaîne de caractères'
	 *
	 * @param resource Url de la ressource(chaîne de caractères)
	 * @return URL associée à la ressource (Not Null)
	 */
	URL resolve(final String resource);

}
