/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamox.search.dsl.rules;

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.OptionalRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamox.search.dsl.model.DslExpression;
import io.vertigo.dynamox.search.dsl.model.DslField;
import io.vertigo.dynamox.search.dsl.model.DslMultiField;
import io.vertigo.dynamox.search.dsl.model.DslQuery;

/**
 * Parsing rule for ListFilterBuidler's expression.
 * (preExpression)(field|multiField):(query)(postExpression)
 * @author npiedeloup
 */
final class DslExpressionRule extends AbstractRule<DslExpression, List<?>> {

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "expression";
	}

	/** {@inheritDoc} */
	@Override
	protected Rule<List<?>> createMainRule() {

		final Rule<List<?>> multiFieldsRule = new SequenceRule(
				DslSyntaxRules.PRE_MODIFIER_VALUE, //0
				new DslMultiFieldRule(), //1
				DslSyntaxRules.POST_MODIFIER_VALUE); //2

		final Rule<Choice> fieldsRule = new FirstOfRule(//"single or multiple")
				new DslFieldRule(), //0
				multiFieldsRule //1
		);
		final Rule<Choice> queriesRule = new FirstOfRule(//"single or multiple")
				new DslTermQueryRule(), //0
				new DslRangeQueryRule(), //1
				new DslMultiQueryRule(), //2
				new DslFixedQueryRule() //3
		);
		return new SequenceRule(
				new OptionalRule<>(new DslBooleanOperatorRule()), //0
				DslSyntaxRules.SPACES, //1
				fieldsRule, //2
				DslSyntaxRules.FIELD_END,
				queriesRule //4
		);
	}

	/** {@inheritDoc} */
	@Override
	protected DslExpression handle(final List<?> parsing) {
		String preExpression = ((Optional<String>) parsing.get(0)).orElse("") + (String) parsing.get(1);
		final String postExpression;
		final Optional<DslField> field;
		final Optional<DslMultiField> multiField;
		final Choice fields = (Choice) parsing.get(2);
		switch (fields.getValue()) {
			case 0:
				field = Optional.of((DslField) fields.getResult());
				multiField = Optional.empty();
				postExpression = "";
				break;
			case 1:
				final List<?> multiFieldParsing = (List<?>) fields.getResult();
				preExpression = DslUtil.concat(preExpression, (String) multiFieldParsing.get(0));
				multiField = Optional.of((DslMultiField) multiFieldParsing.get(1));
				postExpression = (String) multiFieldParsing.get(2);
				field = Optional.empty();
				break;
			default:
				throw new IllegalArgumentException("case " + fields.getValue() + " not implemented");
		}

		final Choice queries = (Choice) parsing.get(4);
		final DslQuery query = (DslQuery) queries.getResult();

		return new DslExpression(preExpression, field, multiField, query, postExpression);
	}
}
