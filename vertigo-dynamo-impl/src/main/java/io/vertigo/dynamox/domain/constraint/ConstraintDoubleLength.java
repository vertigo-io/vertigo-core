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
package io.vertigo.dynamox.domain.constraint;

import io.vertigo.kernel.lang.MessageText;

import java.math.BigDecimal;

/**
 * Contrainte vérifiant que l'objet est : <ul>
 * <li>soit un Double comprit dans le segment ]-10^n, 10^n[</li>
 * <li>soit null </li>
 * </ul>.
 * <br>
 *
 * @author pchretien
 */
public final class ConstraintDoubleLength extends AbstractConstraintLength<Double> {
	/**
	 * Borne maximale au sens strict de Double (= 10 puissance maxLength)
	 */
	private Double maxValue;

	/**
	 * Borne minimale au sens strict de Double (= - maxValue)
	 */
	private Double minValue;

	public ConstraintDoubleLength(final String name) {
		super(name);
	}

	/**
	 * Constructeur nécessaire pour le ksp.
	 * @param args Liste des arguments réduite à un seul castable en integer.
	 * Cet argument correspond au nombre de chiffres maximum autorisé sur le Double.
	 * maxLength Valeur n du segment ]-10^n, 10^n[ dans lequel est comprise la valeur.
	 */
	@Override
	public void initParameters(final String args) {
		setMaxLength(args);
		//----------------------------------------------------------------------
		maxValue = BigDecimal.valueOf(1L).movePointRight(getMaxLength()).doubleValue();
		minValue = BigDecimal.valueOf(1L).movePointRight(getMaxLength()).negate().doubleValue();
	}

	/** {@inheritDoc} */
	public boolean checkConstraint(final Double value) {
		if (value == null) {
			return true;
		}
		return value.compareTo(maxValue) < 0 && value.compareTo(minValue) > 0;
	}

	/** {@inheritDoc} */
	@Override
	protected MessageText getDefaultMessage() {
		return new MessageText(Resources.DYNAMO_CONSTRAINT_DECIMALLENGTH_EXCEEDED, minValue, maxValue);
	}
}
