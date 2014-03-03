package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.OBJECT_END;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.OBJECT_START;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Attribute;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XPropertyEntry;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Règle définissant le corps d'une définition dynamo.
 * Une définition est composée d'une liste de
 * - couple (propriété, valeur)
 * - couple (champ, définition(s)).
 * Une définition étant soit affectée en ligne soit référencée.
 *
 * @author pchretien
 * @version $Id: DefinitionBodyRule.java,v 1.12 2014/01/24 17:59:38 pchretien Exp $
 */
public final class DefinitionBodyRule extends AbstractRule<XDefinitionBody, List<?>> {
	private final DynamicDefinitionRepository dynamicModelRepository;
	private final Entity entity;

	/**
	 * Constructeur.
	 * @param dynamicModelRepository DynamicModelRepository
	 */
	public DefinitionBodyRule(final DynamicDefinitionRepository dynamicModelRepository, final Entity entity) {
		Assertion.checkNotNull(dynamicModelRepository);
		Assertion.checkNotNull(entity);
		//----------------------------------------------------------------------
		this.dynamicModelRepository = dynamicModelRepository;
		this.entity = entity;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "definition<" + entity.getName() + ">";
	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final List<String> attributeNames = new ArrayList<>();

		final List<Rule<?>> innerDefinitionRules = new ArrayList<>();

		for (final Attribute attribute : entity.getAttributes()) {
			final String attributeName = attribute.getName();
			attributeNames.add(attributeName);
			innerDefinitionRules.add(new InnerDefinitionRule(dynamicModelRepository, attributeName, attribute.getEntity()));
		}

		final XPropertyEntryRule xPropertyEntryRule = new XPropertyEntryRule(entity.getProperties());
		final XDefinitionEntryRule xDefinitionEntryRule = new XDefinitionEntryRule(attributeNames);
		final FirstOfRule firstOfRule = new FirstOfRule(//
				xPropertyEntryRule, // 0
				xDefinitionEntryRule, // 1
				new FirstOfRule(innerDefinitionRules),//2, 
				SPACES);

		final ManyRule<Choice> manyRule = new ManyRule<>(firstOfRule, true);
		return new SequenceRule(//
				OBJECT_START,//
				SPACES,//
				manyRule,//2
				SPACES,//
				OBJECT_END//
		);
	}

	@Override
	protected XDefinitionBody handle(final List<?> parsing) {
		final List<Choice> many = (List<Choice>) parsing.get(2);

		final List<XDefinitionEntry> fieldDefinitionEntries = new ArrayList<>();
		final List<XPropertyEntry> fieldPropertyEntries = new ArrayList<>();
		for (final Choice item : many) {
			switch (item.getValue()) {
				case 0:
					//Soit on est en présence d'une propriété standard
					final XPropertyEntry propertyEntry = (XPropertyEntry) item.getResult();
					fieldPropertyEntries.add(propertyEntry);
					break;
				case 1:
					final XDefinitionEntry xDefinitionEntry = (XDefinitionEntry) item.getResult();
					fieldDefinitionEntries.add(xDefinitionEntry);
					break;
				case 2:
					final Choice subTuple = (Choice) item.getResult();
					fieldDefinitionEntries.add((XDefinitionEntry) subTuple.getResult());
					break;
				case 3:
					break;
				default:
					throw new IllegalArgumentException("Type of rule not supported");
			}
		}
		return new XDefinitionBody(fieldDefinitionEntries, fieldPropertyEntries);
	}
}
