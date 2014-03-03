package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XDefinitionEntry;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/*
 * @author pchretien
 * @version $Id: DynamicDefinitionRule.java,v 1.6 2014/01/24 17:59:38 pchretien Exp $
 */
public final class DynamicDefinitionRule extends AbstractRule<DynamicDefinition, Choice> {
	/** Création de la définition. */
	private final DynamicDefinitionRepository dynamicModelRepository;
	private final String operation;

	/**
	 * Constructeur.
	 * 
	 * @param packageName Nom du package
	 * @param dynamicModelRepository DynamicModelRepository
	 */
	public DynamicDefinitionRule(final String operation, final DynamicDefinitionRepository dynamicModelRepository) {
		Assertion.checkArgNotEmpty(operation);
		Assertion.checkNotNull(dynamicModelRepository);
		// ----------------------------------------------------------------------
		this.operation = operation;
		this.dynamicModelRepository = dynamicModelRepository;
	}

	private Rule<List<?>> createRule(final InnerDefinitionRule definitionRule) {
		// Création de la règle de déclaration d'une nouvelle definition.
		return new SequenceRule(//Definition
				new TermRule(operation),// alter ou create
				SPACES,//
				definitionRule,//2
				SPACES//
		);
	}

	@Override
	protected Rule<Choice> createMainRule() {
		final List<Rule<?>> rules = new ArrayList<>();//"Definition")
		for (final Entity entity : dynamicModelRepository.getGrammar().getEntities()) {
			final InnerDefinitionRule definitionRule = new InnerDefinitionRule(dynamicModelRepository, entity.getName(), entity);
			rules.add(createRule(definitionRule));
		}
		return new FirstOfRule(rules);
	}

	@Override
	protected DynamicDefinition handle(final Choice parsing) {
		final XDefinitionEntry xDefinitionEntry = (XDefinitionEntry) ((List) parsing.getResult()).get(2);
		return xDefinitionEntry.getDefinition();
	}
}
