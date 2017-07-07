/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account.impl.authorization.dsl.rules;

import java.util.List;

import io.vertigo.account.authorization.metamodel.rulemodel.DslExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.DslExpression.ValueOperator;
import io.vertigo.account.authorization.metamodel.rulemodel.DslValue;
import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;

/**
 * Parsing rule for SecurityRuleBuidler's expression.
 * (field)(operator)(value)
 * @author npiedeloup
 */
final class DslExpressionRule extends AbstractRule<DslExpression, List<Object>> {

	DslExpressionRule() {
		super(createMainRule(), "expression");
	}

	private static PegRule<List<Object>> createMainRule() {

		final PegRule<PegChoice> valuesRule = PegRules.choice(//"single or multiple")
				new DslUserPropertyValueRule(), //0
				new DslFixedValueRule() //1
		);
		return PegRules.sequence(
				DslSyntaxRules.SPACES, //0
				DslSyntaxRules.WORD, //1
				DslSyntaxRules.SPACES, //2
				new DslOperatorRule<>(ValueOperator.values(), "operator"), //3
				DslSyntaxRules.SPACES, //4
				valuesRule, //5
				DslSyntaxRules.SPACES); //6
	}

	/** {@inheritDoc} */
	@Override
	protected DslExpression handle(final List<Object> parsing) {
		final String fieldName = (String) parsing.get(1);
		final ValueOperator operator = (ValueOperator) parsing.get(3);
		final DslValue value = (DslValue) ((PegChoice) parsing.get(5)).getValue();
		return new DslExpression(fieldName, operator, value);
	}
}
