package io.vertigo.dynamo.impl.environment.kernel.meta;

import java.util.Collections;

/**
 * Fournisseur d'une grammaire spécifique.
 * 
 * @author pchretien
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
