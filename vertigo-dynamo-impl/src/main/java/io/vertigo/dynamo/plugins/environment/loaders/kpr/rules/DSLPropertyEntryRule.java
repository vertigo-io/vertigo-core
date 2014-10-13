/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DSLSyntaxRules.PAIR_SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DSLSyntaxRules.PROPERTY_VALUE;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DSLSyntaxRules.QUOTATION_MARK;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DSLSyntaxRules.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.util.StringUtil;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DSLPropertyEntry;

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
public final class DSLPropertyEntryRule extends AbstractRule<DSLPropertyEntry, List<?>> {
	private final Map<String, EntityProperty> entityProperties;

	/**
	 * <propertyName> : "<propertyvalue>";
	 */
	public DSLPropertyEntryRule(final Set<EntityProperty> entityProperties) {
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
				new OptionRule<>(DSLSyntaxRules.OBJECT_SEPARATOR)//
		);
	}

	@Override
	protected DSLPropertyEntry handle(final List<?> parsing) {
		final String propertyName = (String) ((Choice) parsing.get(0)).getResult();
		final String propertyValue = (String) parsing.get(5);
		return new DSLPropertyEntry(entityProperties.get(propertyName), propertyValue);
	}
}
