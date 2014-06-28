package io.vertigo.quarto.publisher.impl.merger.grammar;

import io.vertigo.quarto.publisher.impl.merger.script.ScriptGrammar;
import io.vertigo.quarto.publisher.impl.merger.script.ScriptTag;

/**
 * Grammaire des �ditions.
 * Offre un langage simple et de haut niveau permettant d'utiliser une syntaxe non java
 * afin par exemple de constituer des �ditions.
 *
 * Une grammaire est constitu�e de mots cl�s (Keyword) en nombre fini.
 *
 * @author oboitel, pchretien
 * @version $Id: ScriptGrammarUtil.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public final class ScriptGrammarUtil {

	/*
	 * Mots cl�s de la grammaire de base.
	 */
	private static enum Keyword {
		/**
		 * FIELD.
		 */
		FIELD("=", false, TagEncodedField.class),
		//FIELD("=", false, TagField.class),
		/**
		 * IF.
		 */
		IF("if ", true, TagIf.class),
		/**
		 * IFNOT.
		 */
		IFNOT("ifnot ", true, TagIfNot.class),
		/**
		 * IFEQUALS.
		 */
		IFEQUALS("ifequals ", true, TagIfEquals.class),
		/**
		 * IFNOT.
		 */
		IFNOTEQUALS("ifnotequals ", true, TagIfNotEquals.class),
		/**
		 * FOR.
		 */
		FOR("loop ", true, TagFor.class),
		/**
		 * BLOCK.
		 */
		BLOCK("block ", true, TagBlock.class),
		/**
		 * OBJECT.
		 */
		OBJECT("var ", true, TagObject.class);

		private final String syntax;
		private final boolean hasBody;
		private final Class<?> tagClass;

		private <S extends ScriptTag> Keyword(final String syntax, final boolean hasBody, final Class<S> tagClass) {
			this.syntax = syntax;
			this.hasBody = hasBody;
			this.tagClass = tagClass;
		}

		String getSyntax() {
			return syntax;
		}

		boolean hasBody() {
			return hasBody;
		}

		<S extends ScriptTag> Class<S> getTagClass() {
			return (Class<S>) tagClass;
		}
	}

	private ScriptGrammarUtil() {
		// Class utilitaire sans �tat
	}

	/**
	 * @return ScriptGrammar initialis�.
	 */
	public static ScriptGrammar createScriptGrammar() {
		final ScriptGrammar scriptGrammar = new ScriptGrammar();
		//On enregistre tous les mots cl�s.
		for (final Keyword keyword : Keyword.values()) {
			registerScriptTag(scriptGrammar, keyword);
		}
		return scriptGrammar;
	}

	private static void registerScriptTag(final ScriptGrammar scriptGrammar, final Keyword keyword) {
		scriptGrammar.registerScriptTag(keyword.getSyntax(), keyword.getTagClass(), keyword.hasBody());
	}

}
