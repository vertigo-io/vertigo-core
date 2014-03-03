package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.commons.parser.WhiteSpaceRule;
import io.vertigo.commons.parser.WordRule;

import java.util.List;

/**
 *
 * Les lettres interdites dans les mots sont les suivantes espace
 * =();[]"/.
 * 
 * @author pchretien
 */
final class Syntax {
	/** Liste des caractères réservés. */
	private static final String RESERVED = "\"=();[]/,{}:";
	/** Liste des caractères blancs. */
	private static final String WHITE_SPACE = " \t\n\r";
	/** Liste des délimiteurs. */
	private static final String DELIMITERS = RESERVED + WHITE_SPACE;

	/** règle de suppression des blancs. */
	static final Rule<?> SPACES = new WhiteSpaceRule(WHITE_SPACE);
	static final Rule<String> SEPARATOR = new TermRule(";");

	static final Rule<String> ARRAY_START = new TermRule("{");
	static final Rule<String> ARRAY_END = new TermRule("}");
	static final Rule<String> ARRAY_SEPARATOR = new TermRule(",");

	static final Rule<String> OBJECT_START = new TermRule("(");
	static final Rule<String> OBJECT_END = new TermRule(")");

	static final Rule<String> PAIR_SEPARATOR = new TermRule(":"); //name:"bill" 
	static final Rule<String> QUOTATION_MARK = new TermRule("\"");

	static final Rule<String> PROPERTY_VALUE = new WordRule(false, "\"", WordRule.Mode.REJECT_ESCAPABLE); //En fait il faut autoriser tous les caractères sauf les guillemets".
	//Il faut gérer le caractère d'évitement.
	static final Rule<String> WORD = new WordRule(false, DELIMITERS, WordRule.Mode.REJECT, "DELIMITERS");
	static final Rule<List<String>> WORDS = new WordListRule();

	private Syntax() {
		//Classe sans état
	}

}
