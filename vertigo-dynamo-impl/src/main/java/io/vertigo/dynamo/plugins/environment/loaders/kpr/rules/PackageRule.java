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
