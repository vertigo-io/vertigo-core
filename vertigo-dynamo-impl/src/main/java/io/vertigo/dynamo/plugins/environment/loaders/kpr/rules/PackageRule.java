package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.WORD;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;

import java.util.List;

/**
 * règle de déclaration d'un package.
 *
 * règle spécifiant qu'un package doit commencer par :
 * package nomdupackage;.
 * @author pchretien
 */
public final class PackageRule extends AbstractRule<String, List<?>> {
	@Override
	protected Rule<List<?>> createMainRule() {
		return new SequenceRule(//
				new TermRule("package "),//après package il y a un blanc obligatoire
				SPACES,//
				WORD,// Nom du package 2
				SPACES,//
				SEPARATOR);
	}

	@Override
	protected String handle(final List<?> parsing) {
		return (String) parsing.get(2); //Indice de la règle packageNamerule
	}
}
