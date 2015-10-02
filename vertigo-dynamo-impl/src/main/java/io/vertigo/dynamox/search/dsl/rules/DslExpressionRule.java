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
package io.vertigo.dynamox.search.dsl.rules;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamox.search.dsl.definition.DslExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslFieldDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiFieldDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslQueryDefinition;
import io.vertigo.lang.Option;

import java.util.List;

/**
 * Parsing rule for ListFilterBuidler's expression.
 * (preExpression)(field|multiField):(query)(postExpression)
 * @author npiedeloup
 */
final class DslExpressionRule extends AbstractRule<DslExpressionDefinition, List<?>> {

	@Override
	public String getExpression() {
		return "expression";
	}

	@Override
	protected Rule<List<?>> createMainRule() {

		final Rule<List<?>> multiFieldsRule = new SequenceRule(
				DslSyntaxRules.PRE_MODIFIER_VALUE, //0
				new DslMultiFieldRule(),//1
				DslSyntaxRules.PRE_MODIFIER_VALUE); //2

		final Rule<Choice> fieldsRule = new FirstOfRule(//"single or multiple")
				new DslFieldRule(), //0
				multiFieldsRule //1
		);
		final Rule<Choice> queriesRule = new FirstOfRule(//"single or multiple")
				new DslTermQueryRule(), //0
				new DslRangeQueryRule(), //1
				new DslMultiQueryRule(0), //2
				new DslFixedQueryRule() //3
		);
		return new SequenceRule(
				DslSyntaxRules.SPACES, //0
				fieldsRule,//1
				DslSyntaxRules.FIELD_END,
				queriesRule, //3
				DslSyntaxRules.SPACES); //4
	}

	@Override
	protected DslExpressionDefinition handle(final List<?> parsing) {
		String preExpression = (String) parsing.get(0);
		String postExpression = (String) parsing.get(4);
		final Option<DslFieldDefinition> field;
		final Option<DslMultiFieldDefinition> multiField;
		final Choice fields = (Choice) parsing.get(1);
		switch (fields.getValue()) {
			case 0:
				field = Option.some((DslFieldDefinition) fields.getResult());
				multiField = Option.none();
				break;
			case 1:
				final List<?> multiFieldParsing = (List<?>) fields.getResult();
				preExpression = DslUtil.concat(preExpression, (String) multiFieldParsing.get(0));
				multiField = Option.some((DslMultiFieldDefinition) multiFieldParsing.get(1));
				postExpression = DslUtil.concat((String) multiFieldParsing.get(2), postExpression);
				field = Option.none();
				break;
			default:
				throw new IllegalArgumentException("case " + fields.getValue() + " not implemented");
		}

		final Choice queries = (Choice) parsing.get(3);
		final DslQueryDefinition query = (DslQueryDefinition) queries.getResult();

		return new DslExpressionDefinition(preExpression, field, multiField, query, postExpression);
	}
}
