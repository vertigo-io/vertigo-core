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
package io.vertigo.commons.parser;

import io.vertigo.lang.Assertion;

import java.util.List;

/**
 * Règle ET.
 * Toutes les règles ajoutées avant l'évaluation doivent être vérifiées.
 * On remonte une erreur dés qu'une seule des règles n'est pas suivie.
 * L'erreur précise le début du bloc ET et précise la cause. (C'est à dire l clause et qui n'est pas suivie).
 * @author pchretien
 * @param<R> Type of the product text parsing
 */
public final class ManyRule<R> implements Rule<List<R>> {
	private final Rule<R> rule;
	private final boolean emptyAccepted;

	private final boolean repeat;

	/**
	 * Constructeur.
	 * @param emptyAccepted Si liste vide autorisée
	 */
	public ManyRule(final Rule<R> rule, final boolean emptyAccepted, final boolean repeat) {
		Assertion.checkNotNull(rule);
		//-----
		this.rule = rule;
		this.emptyAccepted = emptyAccepted;
		this.repeat = repeat;
	}

	/**
	 * Constructeur.
	 * @param emptyAccepted Si liste vide autorisée
	 */
	public ManyRule(final Rule<R> rule, final boolean emptyAccepted) {
		this(rule, emptyAccepted, false);
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "(" + rule.getExpression() + ")" + (emptyAccepted ? "*" : "+");
	}

	boolean isEmptyAccepted() {
		return emptyAccepted;
	}

	boolean isRepeat() {
		return repeat;
	}

	Rule<R> getRule() {
		return rule;
	}

	/** {@inheritDoc} */
	@Override
	public Parser<List<R>> createParser() {
		return new ManyRuleParser<>(this);
	}
}
