package io.vertigo.dynamo.plugins.collections.lucene;

import io.vertigo.kernel.lang.MessageKey;

/**
 * Dictionnaire des ressources.
 *
 * @author  npiedeloup
 * @version $Id: Resources.java,v 1.2 2013/10/22 12:34:46 pchretien Exp $
*/
public enum Resources implements MessageKey {
	/**
	 * "Votre recherche n'est pas assez sélective. Merci de préciser plus de lettres.".
	 */
	DYNAMO_COLLECTIONS_INDEXER_TOO_MANY_CLAUSES,
}
