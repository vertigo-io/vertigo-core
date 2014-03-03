package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.PAIR_SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.WORD;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.WORDS;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XDefinitionEntry;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * règle de déclaration d'une champ référenéant une listes de clés.
 * @author pchretien
 * @version $Id: XDefinitionEntryRule.java,v 1.6 2014/01/24 17:59:38 pchretien Exp $
 */
public final class XDefinitionEntryRule extends AbstractRule<XDefinitionEntry, List<?>> {
	private final List<String> fieldNames;

	/**
	 * Constructeur.
	 */
	public XDefinitionEntryRule(final List<String> fieldNames) {
		Assertion.checkNotNull(fieldNames);
		//----------------------------------------------------------------------
		this.fieldNames = fieldNames;

	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final List<Rule<?>> fieldNamesRules = new ArrayList<>();
		for (final String fieldName : fieldNames) {
			fieldNamesRules.add(new TermRule(fieldName));
		}
		//---------------------------------------------------------------------
		return new SequenceRule(//"DefinitionKey"
				new FirstOfRule(fieldNamesRules), //0
				SPACES,//
				PAIR_SEPARATOR,//
				SPACES,//
				new FirstOfRule(WORD, WORDS),//4
				SPACES,//
				new OptionRule<>(SEPARATOR)//
		);
	}

	@Override
	protected XDefinitionEntry handle(final List<?> parsing) {
		final String fieldName = (String) ((Choice) parsing.get(0)).getResult();
		final List<String> definitionKeys;

		final Choice definitionChoice = (Choice) parsing.get(4);
		switch (definitionChoice.getValue()) {
			case 1:
				//Déclaration d'une liste de définitions identifiée par leurs clés
				definitionKeys = (List<String>) definitionChoice.getResult();
				break;
			case 0:
				//Déclaration d'une définition identifiée par sa clé
				final String value = (String) definitionChoice.getResult();
				definitionKeys = java.util.Collections.singletonList(value);
				break;
			default:
				throw new IllegalStateException();
		}
		return new XDefinitionEntry(fieldName, definitionKeys);
	}
}
