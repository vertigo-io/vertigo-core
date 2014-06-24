package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.PAIR_SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.PROPERTY_VALUE;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.QUOTATION_MARK;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XPropertyEntry;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * règle de déclaration d'une affectation de propriété.
 * Dans la mesure ou l'on récupère un couple propriété, valeur on apparente
 * cela à une Entry (voir api Map).
 *
 * La propriété doit exister.
 * Syntaxe : nomPropriété: "valeur";
 * Exemple : maxLength:"10";
 *
 * @author pchretien
 */
public final class XPropertyEntryRule extends AbstractRule<XPropertyEntry, List<?>> {
	private final Map<String, EntityProperty> entityProperties;

	/**
	 * <propertyName> : "<propertyvalue>";
	 */
	public XPropertyEntryRule(final Set<EntityProperty> entityProperties) {
		super();
		Assertion.checkNotNull(entityProperties);
		//----------------------------------------------------------------------
		this.entityProperties = new HashMap<>();
		for (final EntityProperty entityProperty : entityProperties) {
			final String propertyName = StringUtil.constToCamelCase(entityProperty.getName(), false);
			this.entityProperties.put(propertyName, entityProperty);
		}
	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final List<Rule<?>> propertyNamesRules = new ArrayList<>();
		for (final String propertyName : entityProperties.keySet()) {
			propertyNamesRules.add(new TermRule(propertyName));
		}

		return new SequenceRule(//
				new FirstOfRule(propertyNamesRules),//
				SPACES,//
				PAIR_SEPARATOR,//
				SPACES,//
				QUOTATION_MARK,//
				PROPERTY_VALUE,//5
				QUOTATION_MARK,//
				SPACES,//
				new OptionRule<>(Syntax.OBJECT_SEPARATOR)//
		);
	}

	@Override
	protected XPropertyEntry handle(final List<?> parsing) {
		final String propertyName = (String) ((Choice) parsing.get(0)).getResult();
		final String propertyValue = (String) parsing.get(5);
		return new XPropertyEntry(entityProperties.get(propertyName), propertyValue);
	}
}
