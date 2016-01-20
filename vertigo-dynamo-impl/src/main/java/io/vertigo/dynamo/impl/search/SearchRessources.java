package io.vertigo.dynamo.impl.search;

import io.vertigo.lang.MessageKey;

/**
 * Dictionnaire des ressources.
 *
 * @author  npiedeloup
 */
public enum SearchRessources implements MessageKey {
	/**
	 * Search syntax error.\nDon't use ( ) [ ] or check you closed them. OR and AND are supported but must be between two keywords.
	 */
	DYNAMO_SEARCH_QUERY_SYNTAX_ERROR,

}
