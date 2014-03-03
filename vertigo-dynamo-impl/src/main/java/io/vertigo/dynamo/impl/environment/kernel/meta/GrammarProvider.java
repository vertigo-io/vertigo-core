package io.vertigo.dynamo.impl.environment.kernel.meta;

import java.util.Collections;

/**
 * Fournisseur d'une grammaire spécifique.
 * 
 * @author pchretien
 * @version $Id: GrammarProvider.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public abstract class GrammarProvider {
	private final Grammar grammar = new Grammar(Collections.EMPTY_LIST);

	/**
	 * @return Grammaire générique.
	 */
	public final Grammar getGrammar() {
		return grammar;
	}
}
